CREATE DATABASE LibraryManagementDB;
GO

USE LibraryManagementDB;
GO

-- 1. Bảng NhanVien
CREATE TABLE NhanVien (
    MaNV INT IDENTITY(1,1) PRIMARY KEY,
    HoTen NVARCHAR(100) NOT NULL,
    TenDangNhap VARCHAR(50) NOT NULL UNIQUE,
    MatKhauHash VARCHAR(255) NOT NULL,
    VaiTro NVARCHAR(20) NOT NULL CHECK (VaiTro IN (N'Admin', N'ThuThu')),
    SoDienThoai VARCHAR(15),
    Email VARCHAR(100),
    TrangThai BIT NOT NULL DEFAULT 1 -- 1: Hoạt động, 0: Đã khóa
);
GO

-- 2. Bảng TheLoai
CREATE TABLE TheLoai (
    MaTheLoai INT IDENTITY(1,1) PRIMARY KEY,
    TenTheLoai NVARCHAR(100) NOT NULL UNIQUE,
    MoTa NVARCHAR(255)
);
GO

-- 3. Bảng Sach
CREATE TABLE Sach (
    MaSach INT IDENTITY(1,1) PRIMARY KEY,
    MaTheLoai INT NOT NULL FOREIGN KEY REFERENCES TheLoai(MaTheLoai),
    TenSach NVARCHAR(200) NOT NULL,
    TacGia NVARCHAR(100) NOT NULL,
    NhaXuatBan NVARCHAR(100),
    NamXuatBan INT CHECK (NamXuatBan >= 1900 AND NamXuatBan <= YEAR(GETDATE())),
    ISBN VARCHAR(20) UNIQUE,
    SoLuongTong INT NOT NULL CHECK (SoLuongTong >= 0),
    SoLuongConLai INT NOT NULL CHECK (SoLuongConLai >= 0),
    SoLuongThanhLy INT NOT NULL DEFAULT 0 CHECK (SoLuongThanhLy >= 0),
    TinhTrangSach NVARCHAR(50) NOT NULL DEFAULT N'Bình thường',
    ViTriKe NVARCHAR(50),
    TrangThai BIT NOT NULL DEFAULT 1, -- 1: Đang lưu hành, 0: Đã ẩn/xóa mềm
    CONSTRAINT CK_Sach_SoLuong CHECK (SoLuongConLai + SoLuongThanhLy <= SoLuongTong)
);
GO

-- 4. Bảng DocGia (Bắt buộc có cột NgayHetHan)
CREATE TABLE DocGia (
    MaDocGia INT IDENTITY(1,1) PRIMARY KEY,
    HoTen NVARCHAR(100) NOT NULL,
    NgaySinh DATE,
    GioiTinh NVARCHAR(10) CHECK (GioiTinh IN (N'Nam', N'Nữ', N'Khác')),
    SoDienThoai VARCHAR(15) NOT NULL,
    Email VARCHAR(100),
    DiaChi NVARCHAR(255),
    NgayLapThe DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    NgayHetHan DATE NOT NULL,
    TrangThai BIT NOT NULL DEFAULT 1, -- 1: Hoạt động, 0: Đã khóa
    CONSTRAINT CK_DocGia_HanThe CHECK (NgayHetHan >= NgayLapThe)
);
GO

-- 5. Bảng PhieuMuon
CREATE TABLE PhieuMuon (
    MaPhieuMuon INT IDENTITY(1,1) PRIMARY KEY,
    MaDocGia INT NOT NULL FOREIGN KEY REFERENCES DocGia(MaDocGia),
    MaNV INT NOT NULL FOREIGN KEY REFERENCES NhanVien(MaNV),
    NgayMuon DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    HanTra DATE NOT NULL,
    NgayTraThucTe DATE NULL,
    SoLanGiaHan INT NOT NULL DEFAULT 0 CHECK (SoLanGiaHan >= 0),
    TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang mượn', -- Đang mượn, Đã trả, Quá hạn
    TongTienPhat DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (TongTienPhat >= 0),
    CONSTRAINT CK_PhieuMuon_HanTra CHECK (HanTra >= NgayMuon)
);
GO

-- 6. Bảng ChiTietPhieuMuon
CREATE TABLE ChiTietPhieuMuon (
    MaChiTiet INT IDENTITY(1,1) PRIMARY KEY,
    MaPhieuMuon INT NOT NULL FOREIGN KEY REFERENCES PhieuMuon(MaPhieuMuon),
    MaSach INT NOT NULL FOREIGN KEY REFERENCES Sach(MaSach),
    SoLuong INT NOT NULL CHECK (SoLuong > 0),
    TinhTrangKhiMuon NVARCHAR(100) DEFAULT N'Bình thường',
    TinhTrangKhiTra NVARCHAR(100) NULL, -- Bình thường, Hư hỏng, Mất sách
    TienPhat DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (TienPhat >= 0)
);
GO

-- 7. Bảng QuyDinh
CREATE TABLE QuyDinh (
    MaQuyDinh INT IDENTITY(1,1) PRIMARY KEY,
    SoNgayMuonMacDinh INT NOT NULL CHECK (SoNgayMuonMacDinh > 0),
    SoNgayGiaHanMacDinh INT NOT NULL CHECK (SoNgayGiaHanMacDinh > 0),
    SoLanGiaHanToiDa INT NOT NULL CHECK (SoLanGiaHanToiDa >= 0),
    SoSachToiDa INT NOT NULL CHECK (SoSachToiDa > 0),
    PhatTreMoiNgay BIGINT NOT NULL CHECK (PhatTreMoiNgay >= 0),
    PhatHuHong BIGINT NOT NULL CHECK (PhatHuHong >= 0),
    PhatMatSach BIGINT NOT NULL CHECK (PhatMatSach >= 0)
);
GO

-- ==========================================
-- DỮ LIỆU MẪU ĐỂ TEST
-- ==========================================
INSERT INTO NhanVien (HoTen, TenDangNhap, MatKhauHash, VaiTro, SoDienThoai, Email, TrangThai) VALUES
(N'Lê Hữu Hào', 'admin', '123456', N'Admin', '0901234567', 'hao.admin@library.vn', 1),
(N'Nguyễn Văn Phúc', 'thuthu1', '123456', N'ThuThu', '0907654321', 'phuc.tt@library.vn', 1);

INSERT INTO TheLoai (TenTheLoai, MoTa) VALUES
(N'Công nghệ thông tin', N'Sách lập trình, cơ sở dữ liệu, mạng máy tính'),
(N'Kinh tế', N'Sách quản trị kinh doanh, tài chính, marketing'),
(N'Văn học', N'Tiểu thuyết, truyện ngắn trong và ngoài nước');

INSERT INTO Sach (MaTheLoai, TenSach, TacGia, NhaXuatBan, NamXuatBan, ISBN, SoLuongTong, SoLuongConLai, ViTriKe, TrangThai) VALUES
(1, N'Lập trình Java căn bản', N'Phạm Văn Ất', N'NXB Khoa học & Kỹ thuật', 2022, '978-604-1-00001-1', 10, 10, N'Kệ A1', 1),
(1, N'Hệ quản trị CSDL SQL Server', N'Nguyễn Tiến Dũng', N'NXB Thông tin & Truyền thông', 2023, '978-604-1-00002-2', 8, 8, N'Kệ A2', 1),
(2, N'Quản trị học đại cương', N'Lê Thế Giới', N'NXB Tài chính', 2021, '978-604-1-00003-3', 5, 5, N'Kệ B1', 1),
(3, N'Dế Mèn phiêu lưu ký', N'Tô Hoài', N'NXB Kim Đồng', 2020, '978-604-1-00004-4', 12, 12, N'Kệ C1', 1);

INSERT INTO DocGia (HoTen, NgaySinh, GioiTinh, SoDienThoai, Email, DiaChi, NgayLapThe, NgayHetHan, TrangThai) VALUES
(N'Trần Minh Khôi', '2004-05-12', N'Nam', '0911223344', 'khoi.tm@gmail.com', N'Ninh Kiều, Cần Thơ', '2026-01-10', '2027-01-10', 1),
(N'Nguyễn Thị Lan Anh', '2005-08-20', N'Nữ', '0922334455', 'lananh@gmail.com', N'Cái Răng, Cần Thơ', '2025-01-01', '2026-01-01', 1); -- Thẻ đã hết hạn để test nghiệp vụ mượn

INSERT INTO QuyDinh (SoNgayMuonMacDinh, SoNgayGiaHanMacDinh, SoLanGiaHanToiDa, SoSachToiDa, PhatTreMoiNgay, PhatHuHong, PhatMatSach) VALUES
(14, 7, 1, 5, 5000, 50000, 150000);
GO