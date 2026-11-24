package dto;

public class ChiTietPhieuMuonDTO {
    private int maChiTiet;
    private int maPhieuMuon;
    private int maSach;
    private String tenSach;
    private int soLuong;
    private String tinhTrangKhiMuon;
    private String tinhTrangKhiTra;
    private double tienPhat;

    public ChiTietPhieuMuonDTO() {
    }

    public ChiTietPhieuMuonDTO(int maChiTiet, int maPhieuMuon, int maSach, String tenSach,
                               int soLuong, String tinhTrangKhiMuon, String tinhTrangKhiTra, double tienPhat) {
        this.maChiTiet = maChiTiet;
        this.maPhieuMuon = maPhieuMuon;
        this.maSach = maSach;
        this.tenSach = tenSach;
        this.soLuong = soLuong;
        this.tinhTrangKhiMuon = tinhTrangKhiMuon;
        this.tinhTrangKhiTra = tinhTrangKhiTra;
        this.tienPhat = tienPhat;
    }

    public int getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(int maChiTiet) { this.maChiTiet = maChiTiet; }

    public int getMaPhieuMuon() { return maPhieuMuon; }
    public void setMaPhieuMuon(int maPhieuMuon) { this.maPhieuMuon = maPhieuMuon; }

    public int getMaSach() { return maSach; }
    public void setMaSach(int maSach) { this.maSach = maSach; }

    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public String getTinhTrangKhiMuon() { return tinhTrangKhiMuon; }
    public void setTinhTrangKhiMuon(String tinhTrangKhiMuon) { this.tinhTrangKhiMuon = tinhTrangKhiMuon; }

    public String getTinhTrangKhiTra() { return tinhTrangKhiTra; }
    public void setTinhTrangKhiTra(String tinhTrangKhiTra) { this.tinhTrangKhiTra = tinhTrangKhiTra; }

    public double getTienPhat() { return tienPhat; }
    public void setTienPhat(double tienPhat) { this.tienPhat = tienPhat; }
}