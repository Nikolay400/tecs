package com.kolya.tecs.service;

import com.kolya.tecs.model.Buyer;
import com.kolya.tecs.model.Cart;
import com.kolya.tecs.model.Category;
import com.kolya.tecs.model.Product;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileService {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductService productService;
    @Value("${orders.path}")
    private String ordersPath;
    @Value("${imgs.path}")
    private String imgsPath;

    public AbstractMap.SimpleEntry<String,String> createOrderFile(Buyer buyer, List<Cart> cart) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm");
        String dateStr = formatter.format(new Date());
        String orderId = UUID.randomUUID().toString();
        String fileName = "order_"+dateStr+"_"+ orderId+".txt";

        String fileContent = String.format(
                "Имя: %s; \n" +
                        "Фамилия : %s; \n" +
                        "Email : %s; \n" +
                        "Телефон : %s; \n" +
                        "Aдрес : %s; \n",
                buyer.getName(),buyer.getSurname(),buyer.getEmail(),
                buyer.getPhone(), buyer.getAddress()
        );

        Float total = 0f;

        StringBuilder cartStr = new StringBuilder();
        for (Cart c:cart){
            cartStr.append("\n\n");
            Float cPrice = c.getQuantity()*c.getProduct().getPrice();
            total+=cPrice;
            cartStr.append(c.getProduct().getName());
            cartStr.append(" ");
            cartStr.append(c.getProduct().getPrice())
                    .append(" * ").append(c.getQuantity())
                    .append(" = ").append(cPrice).append(" Р");
        }

        cartStr.append("\n\nИтого: ").append(total).append(" Р");

        fileContent+=cartStr.toString();

        File orderDir = new File(ordersPath);
        if (!orderDir.exists()){
            orderDir.mkdir();
        }

        Path path = Paths.get(ordersPath+"/"+fileName);

        Files.writeString(path, fileContent, StandardCharsets.UTF_8);

        return new AbstractMap.SimpleEntry<>(orderId, fileName);
    }

    public String getCsvResult(List<Product> products) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        CSVPrinter csvPrinter= new CSVPrinter(stringBuilder, CSVFormat.DEFAULT);
        csvPrinter.printRecord(
                "Id",
                "Name",
                "Price",
                "Description",
                "Img",
                "Category",
                "Vendor",
                "Quantity"
        );
        for (Product product : products) {
            csvPrinter.printRecord(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getDescription(),
                    product.getImg(),
                    product.getCategory()!=null?product.getCategory().getId():"",
                    product.getVendor()!=null?product.getVendor().getId():"",
                    product.getQuantity()

            );
        }
        String resultCsv = stringBuilder.toString();
        return resultCsv;
    }

    public void newCsvSave(MultipartFile multipartFile) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        CSVParser csvParser = new CSVParser(reader,CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

        List<CSVRecord> records = csvParser.getRecords();
        for (CSVRecord record:records){
            Product product=new Product();
            product.setId(Long.parseLong(record.get("Id")));
            product.setName(record.get("Name"));
            product.setPrice(Float.parseFloat(record.get("Price")));
            product.setDescription(record.get("Description"));
            product.setImg(record.get("Img"));
            Category category = !Strings.isEmpty(record.get("Category"))
                    ?categoryService.findById(Integer.parseInt(record.get("Category")))
                    :null;
            product.setCategory(category);
            /*Vendor vendor = !Strings.isEmpty(record.get("Vendor"))
                    ?vendorRepo.findById(Integer.parseInt(record.get("Vendor"))).orElse(null)
                    :null;
            product.setVendor(vendor);*/
            product.setQuantity(Integer.parseInt(record.get("Quantity")));

            productService.save(product);
        }
    }

    public void imgsSave(MultipartFile imgsZip) throws IOException {
        File destDir = new File(imgsPath);

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(imgsZip.getInputStream());
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
