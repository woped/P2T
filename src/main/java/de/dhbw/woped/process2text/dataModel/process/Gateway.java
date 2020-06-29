package de.dhbw.woped.process2text.dataModel.process;

public class Gateway extends de.dhbw.woped.process2text.dataModel.process.Element {
    private final int type;

    public Gateway(int id, String label, de.dhbw.woped.process2text.dataModel.process.Lane lane, de.dhbw.woped.process2text.dataModel.process.Pool pool, int type) {
        super(id, label, lane, pool);
        this.type = type;
    }

    public int getType() {
        return type;
    }
}