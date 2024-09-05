package com.example.githubprconsumer.bot;

import com.example.githubprconsumer.bot.api.GithubApiService;
import com.example.githubprconsumer.bot.event.InvalidPermissionEvent;
import com.example.githubprconsumer.github.dto.GithubRepositoryAddRequestDto;
import com.example.githubprconsumer.github.GithubRepositoryService;
import com.example.githubprconsumer.github.event.BotRemoveEvent;
import com.example.githubprconsumer.member.Member;
import com.example.githubprconsumer.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class GithubBotService {

    private final GithubApiService githubApiService;

    private final MemberService memberService;

    private final ApplicationEventPublisher eventPublisher;

    private final GithubRepositoryService githubRepositoryService;

    @Scheduled(fixedRate = 3000)
    public void checkAndApproveInvitations() {
        List<GithubInvitationsInfo> invitations = githubApiService.fetchInvitations();

        if (invitations.isEmpty()) {
            return;
        }

        invitations.forEach(this::approveIfValid);
    }

    private void approveIfValid(GithubInvitationsInfo invitation) {
        if (!invitation.permissions().contains("write")) {
            eventPublisher.publishEvent(new InvalidPermissionEvent(invitation.id(), invitation.inviterInfo().login()));
            return;
        }

        approveInvitations(invitation);
    }

    public void approveInvitations(GithubInvitationsInfo invitation) {
        String inviterLogin = invitation.inviterInfo().login();

        // 가입되지 않은 사용자일 경우
        if (!memberService.existsByLogin(inviterLogin)){
            eventPublisher.publishEvent(new InvalidPermissionEvent(invitation.id(), invitation.inviterInfo().login()));
            return;
        }

        log.info("레포지토리 초대를 수락합니다. 레포지토리 풀네임 : {}", invitation.githubRepositoryInfo().fullName());

        Member member = memberService.getMemberByLogin(inviterLogin);

        // 2. 만약 등록된 사용자라면 레포지토리를 등록한다. 등록자는 inviter 이름으로 한다.
        githubApiService.approveInvitation(invitation.id());
        githubRepositoryService.addGithubRepository(new GithubRepositoryAddRequestDto(member.getLogin(), invitation.githubRepositoryInfo().fullName()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void removeSelfFromRepository(BotRemoveEvent botRemoveEvent){
        githubApiService.removeSelfFromRepository(botRemoveEvent.fullName());
    }
}