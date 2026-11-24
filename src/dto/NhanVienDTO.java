package dto;

public class NhanVienDTO {
    private int maNV;
    private String hoTen;
    private String tenDangNhap;
    private String matKhauHash;
    private String vaiTro;
    private String soDienThoai;
    private String email;
    private boolean trangThai;

    public NhanVienDTO() {
    }

    public NhanVienDTO(int maNV, String hoTen, String tenDangNhap, String matKhauHash,
                       String vaiTro, String soDienThoai, String email, boolean trangThai) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.tenDangNhap = tenDangNhap;
        this.matKhauHash = matKhauHash;
        this.vaiTro = vaiTro;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.trangThai = trangThai;
    }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

    public String getMatKhauHash() { return matKhauHash; }
    public void setMatKhauHash(String matKhauHash) { this.matKhauHash = matKhauHash; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
}