package dto;

public class QuyDinhDTO {
    private int maQuyDinh;
    private int soNgayMuonMacDinh;
    private int soNgayGiaHanMacDinh;
    private int soLanGiaHanToiDa;
    private int soSachToiDa;
    private long phatTreMoiNgay;
    private long phatHuHong;
    private long phatMatSach;

    public QuyDinhDTO() {
        this.maQuyDinh = 1;
        this.soNgayMuonMacDinh = 14;
        this.soNgayGiaHanMacDinh = 7;
        this.soLanGiaHanToiDa = 1;
        this.soSachToiDa = 5;
        this.phatTreMoiNgay = 5000L;
        this.phatHuHong = 50000L;
        this.phatMatSach = 150000L;
    }

    public int getMaQuyDinh() { return maQuyDinh; }
    public void setMaQuyDinh(int maQuyDinh) { this.maQuyDinh = maQuyDinh; }

    public int getSoNgayMuonMacDinh() { return soNgayMuonMacDinh; }
    public void setSoNgayMuonMacDinh(int soNgayMuonMacDinh) { this.soNgayMuonMacDinh = soNgayMuonMacDinh; }

    public int getSoNgayGiaHanMacDinh() { return soNgayGiaHanMacDinh; }
    public void setSoNgayGiaHanMacDinh(int soNgayGiaHanMacDinh) { this.soNgayGiaHanMacDinh = soNgayGiaHanMacDinh; }

    public int getSoLanGiaHanToiDa() { return soLanGiaHanToiDa; }
    public void setSoLanGiaHanToiDa(int soLanGiaHanToiDa) { this.soLanGiaHanToiDa = soLanGiaHanToiDa; }

    public int getSoSachToiDa() { return soSachToiDa; }
    public void setSoSachToiDa(int soSachToiDa) { this.soSachToiDa = soSachToiDa; }

    public long getPhatTreMoiNgay() { return phatTreMoiNgay; }
    public void setPhatTreMoiNgay(long phatTreMoiNgay) { this.phatTreMoiNgay = phatTreMoiNgay; }

    public long getPhatHuHong() { return phatHuHong; }
    public void setPhatHuHong(long phatHuHong) { this.phatHuHong = phatHuHong; }

    public long getPhatMatSach() { return phatMatSach; }
    public void setPhatMatSach(long phatMatSach) { this.phatMatSach = phatMatSach; }
}