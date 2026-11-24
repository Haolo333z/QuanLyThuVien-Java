package dto;

public class SachDTO {
    private int maSach;
    private int maTheLoai;
    private String tenTheLoai; // Dùng để hiển thị trên bảng
    private String tenSach;
    private String tacGia;
    private String nhaXuatBan;
    private int namXuatBan;
    private String isbn;
    private int soLuongTong;
    private int soLuongConLai;
    private String viTriKe;
    private boolean trangThai;

    public SachDTO() {
    }

    public SachDTO(int maSach, int maTheLoai, String tenTheLoai, String tenSach, String tacGia,
                   String nhaXuatBan, int namXuatBan, String isbn, int soLuongTong,
                   int soLuongConLai, String viTriKe, boolean trangThai) {
        this.maSach = maSach;
        this.maTheLoai = maTheLoai;
        this.tenTheLoai = tenTheLoai;
        this.tenSach = tenSach;
        this.tacGia = tacGia;
        this.nhaXuatBan = nhaXuatBan;
        this.namXuatBan = namXuatBan;
        this.isbn = isbn;
        this.soLuongTong = soLuongTong;
        this.soLuongConLai = soLuongConLai;
        this.viTriKe = viTriKe;
        this.trangThai = trangThai;
    }

    public int getMaSach() { return maSach; }
    public void setMaSach(int maSach) { this.maSach = maSach; }

    public int getMaTheLoai() { return maTheLoai; }
    public void setMaTheLoai(int maTheLoai) { this.maTheLoai = maTheLoai; }

    public String getTenTheLoai() { return tenTheLoai; }
    public void setTenTheLoai(String tenTheLoai) { this.tenTheLoai = tenTheLoai; }

    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }

    public String getTacGia() { return tacGia; }
    public void setTacGia(String tacGia) { this.tacGia = tacGia; }

    public String getNhaXuatBan() { return nhaXuatBan; }
    public void setNhaXuatBan(String nhaXuatBan) { this.nhaXuatBan = nhaXuatBan; }

    public int getNamXuatBan() { return namXuatBan; }
    public void setNamXuatBan(int namXuatBan) { this.namXuatBan = namXuatBan; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public int getSoLuongTong() { return soLuongTong; }
    public void setSoLuongTong(int soLuongTong) { this.soLuongTong = soLuongTong; }

    public int getSoLuongConLai() { return soLuongConLai; }
    public void setSoLuongConLai(int soLuongConLai) { this.soLuongConLai = soLuongConLai; }

    public String getViTriKe() { return viTriKe; }
    public void setViTriKe(String viTriKe) { this.viTriKe = viTriKe; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
}