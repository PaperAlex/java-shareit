package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private final User user = new User(null, "ru/practicum/shareit/user", "user@mail.ru");
    private final Item item = new Item(null, "item", "cool", true, user, null);

    @BeforeEach
    void setUp() {
        userRepository.save(user);
        itemRepository.save(item);
    }

    @Test
    @DirtiesContext
    void findItemsTest() {
        List<Item> items = itemRepository.findItems("i");

        assertThat(items.get(0).getId(), equalTo(1L));
        assertThat(items.get(0).getName(), equalTo(item.getName()));
        assertThat(items.size(), equalTo(1));
    }
}