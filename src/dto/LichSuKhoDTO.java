package dto;

import java.time.LocalDateTime;

public class LichSuKhoDTO {
    private int maLichSu;
    private int maSach;
    private String tenSach;
    private int maNV;
    private String tenNV;
    private String loaiBienDong;
    private int soLuong;
    private String ghiChu;
    private LocalDateTime ngayThucHien;

    public LichSuKhoDTO() {
    }

    public LichSuKhoDTO(int maLichSu, int maSach, String tenSach, int maNV, String tenNV,
                        String loaiBienDong, int soLuong, String ghiChu, LocalDateTime ngayThucHien) {
        this.maLichSu = maLichSu;
        this.maSach = maSach;
        this.tenSach = tenSach;
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.loaiBienDong = loaiBienDong;
        this.soLuong = soLuong;
        this.ghiChu = ghiChu;
        this.ngayThucHien = ngayThucHien;
    }

    public int getMaLichSu() { return maLichSu; }
    public void setMaLichSu(int maLichSu) { this.maLichSu = maLichSu; }

    public int getMaSach() { return maSach; }
    public void setMaSach(int maSach) { this.maSach = maSach; }

    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getTenNV() { return tenNV; }
    public void setTenNV(String tenNV) { this.tenNV = tenNV; }

    public String getLoaiBienDong() { return loaiBienDong; }
    public void setLoaiBienDong(String loaiBienDong) { this.loaiBienDong = loaiBienDong; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public LocalDateTime getNgayThucHien() { return ngayThucHien; }
    public void setNgayThucHien(LocalDateTime ngayThucHien) { this.ngayThucHien = ngayThucHien; }
}
