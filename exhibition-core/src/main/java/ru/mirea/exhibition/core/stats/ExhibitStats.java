package ru.mirea.exhibition.core.stats;

/**
 * Показатели одного экспоната: число обращений и суммарное время
 * просмотра. Изменять показатели может только служба статистики
 * (метод add доступен внутри пакета), для остальных объект
 * только читается.
 */
public final class ExhibitStats {

    private final int exhibitId;
    private long count;
    private long totalSec;

    ExhibitStats(int exhibitId) {
        this.exhibitId = exhibitId;
    }

    void add(int durationSec) {
        count++;
        totalSec += durationSec;
    }

    public int exhibitId() {
        return exhibitId;
    }

    public long count() {
        return count;
    }

    public long totalSec() {
        return totalSec;
    }

    public double averageSec() {
        return count == 0 ? 0.0 : (double) totalSec / count;
    }
}
