package com.example.githubprconsumer.github.application;

import com.example.githubprconsumer.github.application.dto.BotRemoveEvent;
import com.example.githubprconsumer.github.application.dto.GithubRepositoryAddRequestDto;
import com.example.githubprconsumer.github.application.dto.RepositoryInfo;
import com.example.githubprconsumer.github.domain.Collaborator;
import com.example.githubprconsumer.github.domain.GithubRepository;
import com.example.githubprconsumer.github.domain.GithubRepositoryException;
import com.example.githubprconsumer.github.domain.GithubRepositoryJpaRepository;
import com.example.githubprconsumer.message.application.dto.GithubPRResponse;
import com.example.githubprconsumer.messenger.application.MessengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubRepositoryService {

    private final GithubRepositoryJpaRepository jpaRepository;

    private final CollaboratorService collaboratorService;

    private final MessengerService messengerService;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void addGithubRepository(GithubRepositoryAddRequestDto githubRepositoryAddRequestDto){
        GithubRepository githubRepository = githubRepositoryAddRequestDto.toEntity();
        jpaRepository.save(githubRepository);
        collaboratorService.addCollaborators(githubRepository.getId(), githubRepository.getFullName());
    }

    @Transactional
    public void activateWebhook(RepositoryInfo repositoryInfo){
        GithubRepository githubRepository = jpaRepository.findByFullName(repositoryInfo.fullName()).orElseThrow(
                () -> new GithubRepositoryException.UnsupportedRepositoryException(repositoryInfo.fullName())
        );

        githubRepository.activateWebhook();
    }

    @Transactional
    public void updateAssigneeCount(Long repositoryId, Integer assigneeCount, String login){
        GithubRepository githubRepository = jpaRepository.findById(repositoryId).orElseThrow(
                () -> new GithubRepositoryException.GithubRepositoryNotFoundException(repositoryId)
        );

        if (githubRepository.isNotMine(login)){
            throw new GithubRepositoryException.NotMyGithubRepositoryException(login, githubRepository.getFullName());
        }

        Integer collaboratorCount = collaboratorService.getCollaboratorCount(repositoryId);
        githubRepository.updateAssigneeCount(assigneeCount, collaboratorCount);
    }

    public void sendWebhookNotification(String webhookUrl, GithubPRResponse githubPRResponse){
        GithubRepository githubRepository = jpaRepository.findByWebhookUrl(webhookUrl).orElseThrow(
                () -> new GithubRepositoryException.GithubRepositoryNotFoundException(webhookUrl)
        );

        List<Collaborator> collaborators = collaboratorService.getCollaborators(githubRepository.getId(), githubPRResponse.prAuthor());
        List<String> assigneeLogins = collaborators.stream().map(Collaborator::getLogin).toList();

        // 여기서 노티를 보낸다.
        messengerService.sendMessage(githubRepository.getId(), githubPRResponse, assigneeLogins);
    }

    @Transactional
    public void deleteGithubRepository(Long repositoryId, String login){
        messengerService.deleteAllByRepositoryId(repositoryId, login);
        GithubRepository githubRepository = jpaRepository.findById(repositoryId).orElseThrow(
                () -> new GithubRepositoryException.GithubRepositoryNotFoundException(repositoryId)
        );
        String fullName = githubRepository.getFullName();
        if (githubRepository.isNotMine(login)){
            throw new GithubRepositoryException.NotMyGithubRepositoryException(login, fullName);
        }

        jpaRepository.delete(githubRepository);
        applicationEventPublisher.publishEvent(new BotRemoveEvent(fullName));
    }
}