package de.dhbw.woped.process2text.dataModel.process;

public abstract class Element {
    private final int id;
    private final String label;
    private final de.dhbw.woped.process2text.dataModel.process.Lane lane;
    private final de.dhbw.woped.process2text.dataModel.process.Pool pool;

    Element(int id, String label, de.dhbw.woped.process2text.dataModel.process.Lane lane, de.dhbw.woped.process2text.dataModel.process.Pool pool) {
        this.id = id;
        this.label = label;
        this.lane = lane;
        this.pool = pool;
    }

    public de.dhbw.woped.process2text.dataModel.process.Pool getPool() {
        return pool;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public de.dhbw.woped.process2text.dataModel.process.Lane getLane() {
        return lane;
    }
}