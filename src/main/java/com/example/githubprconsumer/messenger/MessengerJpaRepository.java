package com.example.githubprconsumer.messenger;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessengerJpaRepository extends JpaRepository<Messenger, Long> {

    List<Messenger> findAllByRepositoryId(Long repositoryId);

    Optional<Messenger> findByRepositoryIdAndMessengerType(Long repositoryId, MessengerType messengerType);

    void deleteAllByRepositoryId(Long repositoryId);

}
