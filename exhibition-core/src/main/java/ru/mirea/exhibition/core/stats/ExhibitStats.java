package ru.mirea.exhibition.core.stats;

/**
 * Показатели одного экспоната: число обращений и суммарное время
 * просмотра. Изменять показатели может только служба статистики
 * (метод add доступен внутри пакета), для остальных объект
 * только читается.
 */
public final class ExhibitStats {

    private final int exhibitId;
    private int count;
    private int totalSec;

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

    public int count() {
        return count;
    }

    public int totalSec() {
        return totalSec;
    }

    public double averageSec() {
        return count == 0 ? 0.0 : (double) totalSec / count;
    }
}
