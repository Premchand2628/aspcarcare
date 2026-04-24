package com.carwash.otplogin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SignupRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 60, message = "First name is too long")
    private String firstName;

    @Size(max = 60, message = "Last name is too long")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    @Size(max = 120, message = "Email is too long")
    private String email;

    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 130, message = "Age is out of range")
    private Integer age;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone must be exactly 10 digits")
    private String phone;

    @Size(max = 500, message = "Address is too long")
    private String address;

    @Size(max = 20, message = "Car number is too long")
    private String carNumber;

    @Size(max = 8, message = "Flag is too long")
    private String carAddressDefaultFlag;

    @Size(min = 12, max = 128, message = "Password must be between 12 and 128 characters")
    private String password;
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCarNumber() {
		return carNumber;
	}
	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}
	public String getCarAddressDefaultFlag() {
		return carAddressDefaultFlag;
	}
	public void setCarAddressDefaultFlag(String carAddressDefaultFlag) {
		this.carAddressDefaultFlag = carAddressDefaultFlag;
	}
	@Override
	public String toString() {
		return "SignupRequest [firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + ", age=" + age
				+ ", phone=" + phone + ", address=" + address + ", carNumber=" + carNumber
				+ ", carAddressDefaultFlag=" + carAddressDefaultFlag + ", password=***]";
	}

   
}
