-- Script nâng cấp đồng bộ schema cho database LibraryManagementDB đã tạo trước đó
USE LibraryManagementDB;
GO

-- 1. Đồng bộ bảng Sach (thêm SoLuongThanhLy, TinhTrangSach và cập nhật CHECK số lượng)
IF COL_LENGTH('dbo.Sach', 'SoLuongThanhLy') IS NULL
BEGIN
    ALTER TABLE dbo.Sach
    ADD SoLuongThanhLy INT NOT NULL CONSTRAINT DF_Sach_SoLuongThanhLy DEFAULT 0 WITH VALUES;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_Sach_SoLuongThanhLy' AND parent_object_id = OBJECT_ID('dbo.Sach'))
BEGIN
    ALTER TABLE dbo.Sach ADD CONSTRAINT CK_Sach_SoLuongThanhLy CHECK (SoLuongThanhLy >= 0);
END
GO

IF COL_LENGTH('dbo.Sach', 'TinhTrangSach') IS NULL
BEGIN
    ALTER TABLE dbo.Sach
    ADD TinhTrangSach NVARCHAR(50) NOT NULL CONSTRAINT DF_Sach_TinhTrangSach DEFAULT N'Bình thường' WITH VALUES;
END
GO

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_Sach_SoLuong' AND parent_object_id = OBJECT_ID('dbo.Sach'))
BEGIN
    ALTER TABLE dbo.Sach DROP CONSTRAINT CK_Sach_SoLuong;
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Sach WHERE SoLuongConLai + SoLuongThanhLy > SoLuongTong)
BEGIN
    ALTER TABLE dbo.Sach ADD CONSTRAINT CK_Sach_SoLuong CHECK (SoLuongConLai + SoLuongThanhLy <= SoLuongTong);
END
ELSE
BEGIN
    ALTER TABLE dbo.Sach ADD CONSTRAINT CK_Sach_SoLuong CHECK (SoLuongConLai <= SoLuongTong);
END
GO

-- 2. Đồng bộ bảng PhieuMuon (thêm SoLanGiaHan)
IF COL_LENGTH('dbo.PhieuMuon', 'SoLanGiaHan') IS NULL
BEGIN
    ALTER TABLE dbo.PhieuMuon
    ADD SoLanGiaHan INT NOT NULL CONSTRAINT DF_PhieuMuon_SoLanGiaHan DEFAULT 0 WITH VALUES;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_PhieuMuon_SoLanGiaHan' AND parent_object_id = OBJECT_ID('dbo.PhieuMuon'))
BEGIN
    ALTER TABLE dbo.PhieuMuon ADD CONSTRAINT CK_PhieuMuon_SoLanGiaHan CHECK (SoLanGiaHan >= 0);
END
GO

-- 3. Tạo bảng QuyDinh nếu chưa tồn tại và chèn dữ liệu mặc định
IF OBJECT_ID('dbo.QuyDinh', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.QuyDinh (
        MaQuyDinh INT IDENTITY(1,1) PRIMARY KEY,
        SoNgayMuonMacDinh INT NOT NULL CHECK (SoNgayMuonMacDinh > 0),
        SoNgayGiaHanMacDinh INT NOT NULL CHECK (SoNgayGiaHanMacDinh > 0),
        SoLanGiaHanToiDa INT NOT NULL CHECK (SoLanGiaHanToiDa >= 0),
        SoSachToiDa INT NOT NULL CHECK (SoSachToiDa > 0),
        PhatTreMoiNgay BIGINT NOT NULL CHECK (PhatTreMoiNgay >= 0),
        PhatHuHong BIGINT NOT NULL CHECK (PhatHuHong >= 0),
        PhatMatSach BIGINT NOT NULL CHECK (PhatMatSach >= 0)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.QuyDinh)
BEGIN
    INSERT INTO dbo.QuyDinh (SoNgayMuonMacDinh, SoNgayGiaHanMacDinh, SoLanGiaHanToiDa, SoSachToiDa, PhatTreMoiNgay, PhatHuHong, PhatMatSach)
    VALUES (14, 7, 1, 5, 5000, 50000, 150000);
END
GO

-- 4. Tạo bảng LichSuKhoSach nếu chưa tồn tại
IF OBJECT_ID('dbo.LichSuKhoSach', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.LichSuKhoSach (
        MaLichSu INT IDENTITY(1,1) PRIMARY KEY,
        MaSach INT NOT NULL FOREIGN KEY REFERENCES dbo.Sach(MaSach),
        MaNV INT NOT NULL FOREIGN KEY REFERENCES dbo.NhanVien(MaNV),
        LoaiBienDong NVARCHAR(30) NOT NULL CHECK (LoaiBienDong IN (N'Khởi tạo', N'Nhập thêm', N'Thanh lý', N'Mất sách')),
        SoLuong INT NOT NULL CHECK (SoLuong > 0),
        GhiChu NVARCHAR(255),
        NgayThucHien DATETIME NOT NULL DEFAULT GETDATE()
    );
END
GO

-- 5. Tạo bảng DatTruocSach nếu chưa tồn tại
IF OBJECT_ID('dbo.DatTruocSach', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.DatTruocSach (
        MaDatTruoc INT IDENTITY(1,1) PRIMARY KEY,
        MaDocGia INT NOT NULL FOREIGN KEY REFERENCES dbo.DocGia(MaDocGia),
        MaSach INT NOT NULL FOREIGN KEY REFERENCES dbo.Sach(MaSach),
        MaNV INT NOT NULL FOREIGN KEY REFERENCES dbo.NhanVien(MaNV),
        NgayDat DATETIME NOT NULL DEFAULT GETDATE(),
        HanGiuCho DATE NULL,
        TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang chờ'
            CHECK (TrangThai IN (N'Đang chờ', N'Đã có sách', N'Đã nhận', N'Đã hủy')),
        GhiChu NVARCHAR(255)
    );
END
GO
