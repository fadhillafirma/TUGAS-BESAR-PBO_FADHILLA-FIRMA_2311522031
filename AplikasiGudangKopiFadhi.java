import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

// Interface untuk Manajemen Kopi
interface ManajemenKopi {
    void tambahKopi(String jenisKopi, double berat);

    void lihatStokKopi();

    void perbaruiStokKopi(String jenisKopi, double beratTambahan);

    void hapusKopi(String jenisKopi);
}

// Kelas Super
class Kopi {
    protected String jenisKopi;
    protected double berat;

    public Kopi(String jenisKopi, double berat) {
        this.jenisKopi = jenisKopi;
        this.berat = berat;
    }

    public String getJenisKopi() {
        return jenisKopi;
    }

    public double getBerat() {
        return berat;
    }
}

// Subkelas
class StokKopi extends Kopi {
    private Date terakhirDiperbarui;

    public StokKopi(String jenisKopi, double berat, Date terakhirDiperbarui) {
        super(jenisKopi, berat);
        this.terakhirDiperbarui = terakhirDiperbarui;
    }

    public Date getTerakhirDiperbarui() {
        return terakhirDiperbarui;
    }
}

// Implementasi ManajemenKopi
class GudangKopiFadhi implements ManajemenKopi {
    // Koneksi ke database MySQL yang diakses via phpMyAdmin
    private final String jdbcURL = "jdbc:mysql://localhost:3306/GudangKopiFadhi"; // URL Database
    private final String jdbcUsername = "root"; // Username MySQL
    private final String jdbcPassword = ""; // Kosongkan jika tidak ada password

    public GudangKopiFadhi() {
        try {
            // Memuat driver JDBC MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver JDBC tidak ditemukan.");
        }
    }

    private Connection connect() throws SQLException {
        // Membuat koneksi ke database
        return DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
    }

    @Override
    public void tambahKopi(String jenisKopi, double berat) {
        // Tambahkan kode validasi jenis kopi setelah menerima input
        List<String> jenisKopiValid = Arrays.asList("Arabica", "Robusta", "Luwak", "Excelsa", "Liberika");
        if (!jenisKopiValid.contains(jenisKopi)) {
            System.out.println("Tidak ada jenis biji kopi yang sesuai.");
            return; // Keluar dari metode jika jenis kopi tidak valid
        }

        // Jika valid, lanjutkan ke proses penambahan kopi
        String query = "INSERT INTO StokKopi (jenis_kopi, berat, terakhir_diperbarui) VALUES (?, ?, ?)";
        try (Connection connection = connect(); PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, jenisKopi);
            pstmt.setDouble(2, berat);
            pstmt.setTimestamp(3, new Timestamp(new Date().getTime()));
            pstmt.executeUpdate();
            System.out.println("Kopi berhasil ditambahkan.");
        } catch (SQLException e) {
            System.out.println("Error menambahkan kopi: " + e.getMessage());
        }
    }

    @Override
    public void lihatStokKopi() {
        String query = "SELECT * FROM StokKopi";
        try (Connection connection = connect();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\nStok Kopi:");
            while (rs.next()) {
                String jenisKopi = rs.getString("jenis_kopi");
                double berat = rs.getDouble("berat");
                Timestamp terakhirDiperbarui = rs.getTimestamp("terakhir_diperbarui");
                System.out.printf("Jenis: %s | Berat: %.2f kg | Terakhir Diperbarui: %s\n", jenisKopi, berat,
                        terakhirDiperbarui);
            }
        } catch (SQLException e) {
            System.out.println("Error melihat stok kopi: " + e.getMessage());
        }
    }

    @Override
    public void perbaruiStokKopi(String jenisKopi, double beratTambahan) {
        String query = "UPDATE StokKopi SET berat = berat + ?, terakhir_diperbarui = ? WHERE jenis_kopi = ?";
        try (Connection connection = connect(); PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, beratTambahan);
            pstmt.setTimestamp(2, new Timestamp(new Date().getTime()));
            pstmt.setString(3, jenisKopi);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Stok kopi berhasil diperbarui.");
            } else {
                System.out.println("Jenis kopi tidak ditemukan.");
            }
        } catch (SQLException e) {
            System.out.println("Error memperbarui stok kopi: " + e.getMessage());
        }
    }

    public void kurangiBeratStokKopi(String jenisKopi, double beratKurang) {
        String query = "SELECT berat FROM StokKopi WHERE jenis_kopi = ?";
        try (Connection connection = connect();
                PreparedStatement pstmtSelect = connection.prepareStatement(query)) {

            pstmtSelect.setString(1, jenisKopi);
            ResultSet rs = pstmtSelect.executeQuery();

            if (rs.next()) {
                double beratSekarang = rs.getDouble("berat");
                if (beratKurang > beratSekarang) {
                    System.out.println("Error: Berat yang dikurangi melebihi stok saat ini.");
                } else {
                    String updateQuery = "UPDATE StokKopi SET berat = berat - ?, terakhir_diperbarui = ? WHERE jenis_kopi = ?";
                    try (PreparedStatement pstmtUpdate = connection.prepareStatement(updateQuery)) {
                        pstmtUpdate.setDouble(1, beratKurang);
                        pstmtUpdate.setTimestamp(2, new Timestamp(new Date().getTime()));
                        pstmtUpdate.setString(3, jenisKopi);
                        pstmtUpdate.executeUpdate();
                        System.out.println("Stok kopi berhasil dikurangi.");
                    }
                }
            } else {
                System.out.println("Jenis kopi tidak ditemukan.");
            }
        } catch (SQLException e) {
            System.out.println("Error mengurangi stok kopi: " + e.getMessage());
        }
    }

    @Override
    public void hapusKopi(String jenisKopi) {
        String query = "DELETE FROM StokKopi WHERE jenis_kopi = ?";
        try (Connection connection = connect(); PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, jenisKopi);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Kopi berhasil dihapus.");
            } else {
                System.out.println("Jenis kopi tidak ditemukan.");
            }
        } catch (SQLException e) {
            System.out.println("Error menghapus kopi: " + e.getMessage());
        }
    }
}

// Kelas Utama
public class AplikasiGudangKopiFadhi {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GudangKopiFadhi gudang = new GudangKopiFadhi();

        while (true) {
            System.out.println("\n--- Manajemen Gudang Kopi Fadhi ---");
            System.out.println("1. Tambah Kopi");
            System.out.println("2. Lihat Stok Kopi");
            System.out.println("3. Perbarui Stok Kopi");
            System.out.println("4. Hapus Kopi");
            System.out.println("5. Keluar");
            System.out.print("Pilih opsi: ");
            int pilihan = scanner.nextInt();
            scanner.nextLine();

            switch (pilihan) {
                case 1:
                    System.out.print("Masukkan jenis kopi (Arabica, Robusta, Luwak, Excelsa, Liberika): ");
                    String jenisKopi = scanner.nextLine();

                    // Validasi jenis kopi, jika tidak valid langsung return ke menu utama
                    List<String> jenisKopiValid = Arrays.asList("Arabica", "Robusta", "Luwak", "Excelsa", "Liberika");
                    if (!jenisKopiValid.contains(jenisKopi)) {
                        System.out.println("Tidak ada jenis biji kopi yang sesuai.");
                        break; // Tidak lanjut ke tahap input berat kopi
                    }

                    System.out.print("Masukkan berat (kg): ");
                    double berat = scanner.nextDouble();
                    gudang.tambahKopi(jenisKopi, berat);
                    break;
                case 2:
                    gudang.lihatStokKopi();
                    break;

                case 3:
                    System.out.print("Masukkan jenis kopi yang akan diperbarui: ");
                    String jenisPerbarui = scanner.nextLine();

                    // Memilih apakah menambah atau mengurangi berat
                    System.out.println("Apa yang ingin Anda lakukan?");
                    System.out.println("1. Tambah Berat");
                    System.out.println("2. Kurangi Berat");
                    System.out.print("Pilih opsi: ");
                    int opsi = scanner.nextInt();

                    // Validasi opsi
                    if (opsi == 1) {
                        System.out.print("Masukkan berat tambahan (kg): ");
                        double beratTambahan = scanner.nextDouble();
                        gudang.perbaruiStokKopi(jenisPerbarui, beratTambahan); // Tambah berat
                    } else if (opsi == 2) {
                        System.out.print("Masukkan berat yang akan dikurangi (kg): ");
                        double beratKurang = scanner.nextDouble();

                        // Perhitungan dan pengurangan berat dilakukan dalam metode baru
                        gudang.kurangiBeratStokKopi(jenisPerbarui, beratKurang);
                    } else {
                        System.out.println("Pilihan tidak valid.");
                    }
                    break;
                case 4:
                    System.out.print("Masukkan jenis kopi yang akan dihapus: ");
                    String jenisHapus = scanner.nextLine();
                    gudang.hapusKopi(jenisHapus);
                    break;
                case 5:
                    System.out.println("Keluar... Sampai jumpa!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Pilihan tidak valid. Silakan coba lagi.");
            }
        }
    }
}
