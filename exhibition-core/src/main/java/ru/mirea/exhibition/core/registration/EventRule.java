package ru.mirea.exhibition.core.registration;

import java.util.Optional;

/**
 * Правило проверки события взаимодействия. Каждое правило — отдельный
 * класс; служба регистрации применяет их по порядку и отклоняет
 * событие по первому невыполненному правилу. Новое правило
 * добавляется новым классом без изменения уже существующих.
 */
public interface EventRule {

    /**
     * @return причина отклонения или пусто, если правило выполнено
     */
    Optional<RejectionReason> check(EventCandidate candidate);
}
