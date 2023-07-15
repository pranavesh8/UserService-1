package com.example.controller;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.entity.EXUser;
import com.example.entity.ExceptionResponse;
import com.example.entity.LoginRequest;
import com.example.entity.Partnership;
import com.example.entity.ResponseBean;
import com.example.entity.WebsiteBean;
import com.example.repository.AuthenticationRepository;
import com.example.repository.EXUserRepository;
import com.example.repository.WebsiteBeanRepository;
import com.example.service.EXUserService;
@RestController
@RequestMapping("/exuser")
public class EXUserController {

	@Autowired
	private EXUserRepository userRepo;

	
	@Autowired
	private AuthenticationRepository authonticationRepository;

	@Autowired
	private WebsiteBeanRepository webRepo;
	
	String regex = "^(?=.*[0-9])" + "(?=.*[a-z])(?=.*[A-Z])" + "(?=\\S+$).{8,15}$";

	Pattern p = Pattern.compile(regex);

	
	@Async("asyncExecutor")
	public CompletableFuture<HashMap<String, String>> validateUserConditions(EXUser parent) {
		HashMap<String, String> response = new HashMap<>();
		try {
			if (parent == null) {
				response.put("type", "error");
				response.put("message", "Invalid User Data");
				return CompletableFuture.completedFuture(response);
			}
			if (parent.getEmail() == null || !isValidEmailAddress(parent.getEmail())) {
				response.put("type", "error");
				response.put("message", "Invalid Email Address");
				return CompletableFuture.completedFuture(response);
			} else if (parent.getUserid().equalsIgnoreCase(null) || parent.getUserid().equalsIgnoreCase("")) {
				response.put("type", "error");
				response.put("message", "User Id Required");
				return CompletableFuture.completedFuture(response);
			} else if (parent.getUsername().equalsIgnoreCase("") || parent.getUsername().length() < 1) {
				response.put("type", "error");
				response.put("message", "UserName Must be grater thanv 1 Characters");
				return CompletableFuture.completedFuture(response);
			} else if (p.matcher(parent.getPassword()).matches() == false) {
				response.put("type", "error");
				response.put("message",
						"Password Must contains 1 Upper Case, 1 Lowe Case & 1 Numeric Value & in Between 8-15 Charachter");
				return CompletableFuture.completedFuture(response);
			} else if (parent.getMobileNumber().length() > 10) {
				response.put("type", "error");
				response.put("message", "Mobile Number Must Be Of 10 Digit or Balnk");
				return CompletableFuture.completedFuture(response);
			} else if (parent.getExposureLimit() == null) {
				response.put("type", "error");
				response.put("message", "Invalid Exposure Limit");
				return CompletableFuture.completedFuture(response);
			} else if (parent.getTimeZone().equalsIgnoreCase(null) || parent.getTimeZone().equalsIgnoreCase("")) {
				response.put("type", "error");
				response.put("message", "Invalid TimeZone");
				return CompletableFuture.completedFuture(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("type", "error");
			response.put("message", "Something Went Wrong");
			return CompletableFuture.completedFuture(response);
		}
		response.put("type", "success");
		response.put("message", "Pass");
		return CompletableFuture.completedFuture(response);
	}

	public boolean isValidEmailAddress(String email) {
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
		java.util.regex.Matcher m = p.matcher(email);
		return m.matches();
	}
	 
	@PostMapping("/validateUserCreation")
	public ResponseEntity<Object> validateUserCreation(@RequestBody EXUser requestData) {
		ResponseBean responseBean = new ResponseBean();
		try {
			EXUser checkUser = userRepo.findByUserid(requestData.getUserid().toLowerCase());
			ArrayList isValidUser = new ArrayList<>();

			CompletableFuture<HashMap<String, String>> conditions = validateUserConditions(requestData);
			CompletableFuture.allOf(conditions).join();
			HashMap<String, String> conditionsReturn = new HashMap<>();
			try {
				conditionsReturn = conditions.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (conditionsReturn.containsKey("type") && conditionsReturn.get("type").equalsIgnoreCase("error")) {
				responseBean.setType(conditionsReturn.get("type"));
				responseBean.setMessage(conditionsReturn.get("message"));
				responseBean.setTitle("Error");
				return new ResponseEntity<Object>(responseBean, HttpStatus.ACCEPTED);
			}
			if (requestData.getUsertype() == 0) {
				requestData = saveSubAdmin(requestData);
				if (requestData.getUserid() != null) {
					userRepo.save(requestData);
					responseBean.setType("success");
					responseBean.setMessage("Success");
					responseBean.setTitle("Success");
					return new ResponseEntity<Object>(responseBean, HttpStatus.OK);
				}
			} else if (requestData.getUsertype() == 1) {
				requestData = saveMiniAdmin(requestData);
				if (requestData.getUserid() != null) {
					userRepo.save(requestData);
					responseBean.setType("success");
					responseBean.setMessage("Success");
					responseBean.setTitle("Success");
					return new ResponseEntity<Object>(responseBean, HttpStatus.OK);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		responseBean.setType("error");
		responseBean.setMessage("Not Authorized to Create this type of User");
		responseBean.setTitle("Error");
		return new ResponseEntity<Object>(responseBean, HttpStatus.ACCEPTED);

	}

	public EXUser saveSubAdmin(EXUser parent) {
		EXUser child = new EXUser();
		try {
			child.setUsername(parent.getUsername());
			child.setUserid(parent.getUserid());
			child.setPassword(parent.getPassword());
			child.setUsertype(1);
			child.setAccountLock(false);
			child.setBetLock(false);
			child.setIsActive(true);
			child.setParentId(parent.getId());
			child.setParentName(parent.getUsername());
			child.setParentUserId(parent.getUserid());
			child.setAdminId(parent.getId());
			child.setAdminName(parent.getUsername());
			child.setAdminUserId(parent.getUserid());
			child.setSubadminId("0");
			child.setSubadminName("0");
			child.setSubadminUserId("0");
			child.setMiniadminId("0");
			child.setMiniadminName("0");
			child.setMiniadminUserId("0");
			child.setSupersuperId("0");
			child.setSupersuperName("0");
			child.setSupersuperUserId("0");
			child.setSupermasterId("0");
			child.setSupermasterName("0");
			child.setSupermasterUserId("0");
			child.setMasterId("0");
			child.setMasterName("0");
			child.setMasterUserId("0");
			child.setMyBalance(0.0);
			child.setFixLimit(0.0);
			child.setMyallPl(0.0);
			child.setMysportPl(0.0);
			child.setMycasinoPl(0.0);
			child.setSubChild("0");
			child.setMobileNumber(parent.getMobileNumber());
			child.setIspasswordChanged(false);
			child.setChildLiab(0.0);
			child.setLastName(parent.getLastName());
			child.setTimeZone(parent.getTimeZone());
			child.setEmail(parent.getEmail());
			child.setExposureLimit(0.0);

			Partnership childPartnership = new Partnership();

			childPartnership.setAdminSportPart(100.0);
			childPartnership.setSubadminSportPart(0.0);
			childPartnership.setMiniadminSportPart(0.0);
			childPartnership.setSupermasterSportPart(0.0);
			childPartnership.setSupermasterSportPart(0.0);
			childPartnership.setMasterSportPart(0.0);
			childPartnership.setUserComm(0.0);

			child.setPartnership(childPartnership);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return child;
	}

	public EXUser saveMiniAdmin(EXUser parent) {
		EXUser child = new EXUser();
		try {
			child.setUsername(parent.getUsername());
			child.setUserid(parent.getUserid());
			child.setPassword(parent.getPassword());
			child.setUsertype(2);
			child.setChildLiab(0.0);
			child.setAccountLock(parent.getAccountLock());
			child.setBetLock(parent.getBetLock());
			child.setIsActive(parent.getIsActive());
			child.setAdminId(parent.getAdminId());
			child.setAdminName(parent.getAdminName());
			child.setAdminUserId(parent.getAdminUserId());
			child.setSubadminId(parent.getId());
			child.setSubadminName(parent.getUsername());
			child.setSubadminUserId(parent.getUserid());

			child.setParentId(parent.getId());
			child.setParentName(parent.getUsername());
			child.setParentUserId(parent.getUserid());

			child.setMiniadminId("0");
			child.setMiniadminName("0");
			child.setMiniadminUserId("0");
			child.setSupersuperId("0");
			child.setSupersuperName("0");
			child.setSupersuperUserId("0");
			child.setSupermasterId("0");
			child.setSupermasterName("0");
			child.setSupermasterUserId("0");
			child.setMasterId("0");
			child.setMasterName("0");
			child.setMasterUserId("0");
			child.setMyBalance(0.0);
			child.setFixLimit(0.0);
			child.setMyallPl(0.0);
			child.setMysportPl(0.0);
			child.setMycasinoPl(0.0);
			child.setWebsiteId(parent.getWebsiteId());
			child.setWebsiteName(parent.getWebsiteName());
			child.setSubChild("0");
			child.setMobileNumber(parent.getMobileNumber());
			child.setIspasswordChanged(false);
			child.setLastName(parent.getLastName());
			child.setTimeZone(parent.getTimeZone());
			child.setEmail(parent.getEmail());
			child.setExposureLimit(0.0);

			Partnership childPartnership = new Partnership();

			childPartnership.setAdminSportPart(100.0);
			childPartnership.setSubadminSportPart(0.0);
			childPartnership.setMiniadminSportPart(0.0);
			childPartnership.setSupermasterSportPart(0.0);
			childPartnership.setSupermasterSportPart(0.0);
			childPartnership.setMasterSportPart(0.0);
			childPartnership.setUserComm(0.0);

			child.setPartnership(childPartnership);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return child;
	}
	 

	//Login and authorize*************************************************************************************
	@PostMapping("/managementHome")
	public ResponseEntity<ExceptionResponse> authenticate(@RequestBody LoginRequest user) {

		LoginRequest existingUser = authonticationRepository.findByUserid(user.getUserid());

		// if username null or wrong
		if (existingUser == null) {
			ExceptionResponse newresponse = ExceptionResponse.builder()
					.message("Invalid username. Please check it once").status(HttpStatus.UNAUTHORIZED).succress(false)
					.date(new Date()).time(LocalTime.now()).build();
			return new ResponseEntity<ExceptionResponse>(newresponse, HttpStatus.UNAUTHORIZED);
		}

		// if password wrong
		// The builder pattern allows you to enforce a step-by-step process to construct
		// a complex object as a finished product.
		if (!existingUser.getPassword().equals(user.getPassword())) {
			ExceptionResponse newresponse = ExceptionResponse.builder().message("Invalid password. Please check it")
					.status(HttpStatus.UNAUTHORIZED).succress(false).date(new Date()).time(LocalTime.now()).build();
			return new ResponseEntity<ExceptionResponse>(newresponse, HttpStatus.UNAUTHORIZED);
		}
		// success case
		ExceptionResponse newresponse = ExceptionResponse.builder().message("Authenticated successfully")
				.status(HttpStatus.OK).succress(true).date(new Date()).time(LocalTime.now()).build();
		return new ResponseEntity<ExceptionResponse>(newresponse, HttpStatus.OK);
	}
    //website adding*****************************************************************************************
	@PostMapping("/website")
	public ResponseEntity<ResponseBean> saveWebsite(@RequestBody WebsiteBean website) {
		String name = website.getName();
		if (webRepo.findByName(name) != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ResponseBean("error", "Website already exist!!", "WebSiteBean"));
		} else {
			webRepo.save(website);
		}
		return ResponseEntity.ok(new ResponseBean("Success", "Website Created Successfully!!", "WebSiteBean"));
	}

	//see list of websites*************************************************************************************
	@GetMapping("/allWebsite")
	public List<WebsiteBean> listOfWebsite() {
		List<WebsiteBean> findAll = webRepo.findAll();
		return findAll;
	}

}
