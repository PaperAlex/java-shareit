package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UserMapperTest {
    private final UserDto dto = new UserDto(1L, "user", "user@user.com");
    private final User user = new User(1L, "user", "user@user.com");

    @Test
    public void toUserDto() {
        UserDto userDto = UserMapper.toUserDto(user);
        assertThat(userDto, equalTo(dto));
    }

    @Test
    public void toUser() {
        User user = UserMapper.toUser(dto);
        assertThat(user.getId(), equalTo(this.user.getId()));
        assertThat(user.getName(), equalTo(this.user.getName()));
        assertThat(user.getEmail(), equalTo(this.user.getEmail()));
    }
}