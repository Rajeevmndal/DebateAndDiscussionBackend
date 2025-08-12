package in.Rk.talkForOrAgainst.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    @Id
    private long id;
    private String sender;
    private String content;
    private LocalDateTime timeStamp;
    @ManyToOne
    private Debate debate;

    public Message(String sender,String content){
        this.sender=sender;
        this.content=content;
        this.timeStamp=LocalDateTime.now();
    }

}
