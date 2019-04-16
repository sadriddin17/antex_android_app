package uz.codic.ahmadtea.data.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Price {
    @PrimaryKey(autoGenerate = true)
    int pid;

    int id;
    String label;
    String short_name;
    int currency_id;
    int filial_id;
    int status_id;
    String code;
    String link_id;

    @Ignore
    boolean isChecked;
    public Price(){

    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public int getFilial_id() {
        return filial_id;
    }

    public void setFilial_id(int filial_id) {
        this.filial_id = filial_id;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(int currency_id) {
        this.currency_id = currency_id;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

    public String getLink_id() {
        return link_id;
    }

    public void setLink_id(String link_id) {
        this.link_id = link_id;
    }

    @Override
    public String toString() {
        return "Price{" +
                "pid=" + pid +
                ", id=" + id +
                ", label='" + label + '\'' +
                ", short_name='" + short_name + '\'' +
                ", currency_id=" + currency_id +
                ", filial_id=" + filial_id +
                ", status_id=" + status_id +
                ", code='" + code + '\'' +
                ", link_id='" + link_id + '\'' +
                ", isChecked=" + isChecked +
                '}';
    }
}
