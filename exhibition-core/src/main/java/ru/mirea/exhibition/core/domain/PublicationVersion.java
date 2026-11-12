package ru.mirea.exhibition.core.domain;

/**
 * Версия сведений об экспонате. У опубликованной версии есть период
 * публикации, у черновика его нет (period == null): черновик
 * посетители не видят.
 *
 * @param number номер версии, который стенд хранит в своём кэше
 */
public record PublicationVersion(int id, int exhibitId, int number,
                                 Period period) implements Periodic {

    public boolean isDraft() {
        return period == null;
    }
}
