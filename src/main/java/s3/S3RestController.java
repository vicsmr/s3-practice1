package s3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/buckets")
public class S3RestController {

	public static AmazonS3 s3;

	@GetMapping("/")
	public ResponseEntity<List<Bucket>> listBuckets() {
		configureS3();
		List<Bucket> buckets = s3.listBuckets();
		return new ResponseEntity<>(buckets, HttpStatus.OK);
	}

	@GetMapping("/{bucketName}")
	public ResponseEntity<Bucket> getBucket(@PathVariable String bucketName) {
		configureS3();
		if (!s3.doesBucketExistV2(bucketName)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Bucket bucket = new Bucket();
		List<Bucket> buckets = s3.listBuckets();
		for (Bucket bucketSearch : buckets) {
			if (bucketSearch.getName().equals(bucketName)) {
				bucket = bucketSearch;
			}
		}
		return new ResponseEntity<>(bucket, HttpStatus.OK);
	}

	@PostMapping("/{bucketName}")
	public ResponseEntity<String> newBucket(@PathVariable String bucketName) {
		configureS3();
		if (s3.doesBucketExistV2(bucketName)) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		s3.createBucket(bucketName);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@PostMapping("/{bucketName}/uploadObject")
	public ResponseEntity<File> newObject(@PathVariable String bucketName,
			@RequestParam(value = "file") MultipartFile uploadObject,
			@RequestParam(value = "isPublic") boolean isPublic) throws IOException {
		configureS3();
		if (!s3.doesBucketExistV2(bucketName)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		File convUploadObject = convertMultipartFileToFile(uploadObject);
		PutObjectRequest por = new PutObjectRequest(
				// BUCKET NAME
				bucketName,
				// FILE NAME
				uploadObject.getOriginalFilename(),
				// FILE DATA
				convUploadObject);
		if (isPublic) {
			por.setCannedAcl(CannedAccessControlList.PublicRead);
		}
		s3.putObject(por);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@DeleteMapping("/{bucketName}")
	public ResponseEntity<String> deleteBucket(@PathVariable String bucketName) {
		configureS3();
		if (!s3.doesBucketExistV2(bucketName)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		s3.deleteBucket(bucketName);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@DeleteMapping("/{bucketName}/{objectName}")
	public ResponseEntity<String> deleteComment(@PathVariable String bucketName, @PathVariable String objectName) {
		configureS3();
		if (!s3.doesBucketExistV2(bucketName)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		s3.deleteObject(bucketName, objectName);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public static void configureS3() {
		s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
	}

	public static File convertMultipartFileToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		convFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}

}
