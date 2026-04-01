package com.carwash.otplogin.dto;

public class SignupRequest {
    private String firstName;
    private String lastName;
    private String email;
    private Integer age;
    private String phone;
	private String address;
	private String carNumber;
	private String carAddressDefaultFlag;
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
				+ ", carAddressDefaultFlag=" + carAddressDefaultFlag + ", password=" + password + "]";
	}

   
}
