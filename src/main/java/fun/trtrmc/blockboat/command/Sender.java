package fun.trtrmc.blockboat.command;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Sender {
    private String nickname;
    private String card;
    @Getter
    private String role;
    @Getter
    private String user_id;
    private String title;

    public Sender(String nickname, String card, String role, String user_id, String title) {
        this.nickname = nickname;
        this.card = (Objects.equals(card, "")) ? nickname : card;
        this.role = role;
        this.user_id = user_id;
        this.title = title;
    }
}
