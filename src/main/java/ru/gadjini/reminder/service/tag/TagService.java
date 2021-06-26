package ru.gadjini.reminder.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.TagDao;
import ru.gadjini.reminder.domain.Tag;

import java.util.List;

@Service
public class TagService {

    private TagDao tagDao;

    @Autowired
    public TagService(TagDao tagDao) {
        this.tagDao = tagDao;
    }

    public List<Tag> tags(long userId) {
        return tagDao.tags(userId);
    }

    public void create(long userId, String text) {
        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setText(text);
        tagDao.createTag(tag);
    }
}
