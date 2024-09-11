package com.example.githubprconsumer.messenger.presentation;

import com.example.githubprconsumer.messenger.application.MessengerAddRequestDto;
import com.example.githubprconsumer.messenger.application.MessengerResponseDto;
import com.example.githubprconsumer.messenger.application.MessengerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessengerController {

    private final MessengerService messengerService;

    @PostMapping("/messengers")
    @Operation(description = "레포지토리 웹훅을 수신할 새 메신저를 추가합니다.")
    public MessengerResponseDto addMessenger(@RequestBody MessengerAddRequestDto messengerAddRequestDto){
        return messengerService.addNewMessenger(messengerAddRequestDto);
    }

    @GetMapping("/{encryptedWebhookUrl}")
    public void activateMessenger(@PathVariable String encryptedWebhookUrl){
        messengerService.activateMessenger(encryptedWebhookUrl);
        // TODO : 여기서, 생성된 랜덤 URL 을 ... 리다이렉트를 어떻게할지?
    }

    @DeleteMapping("/messengers/{messengerId}")
    public void deleteMessenger(@PathVariable Long messengerId){
        messengerService.deleteMessenger(messengerId);
    }
}
