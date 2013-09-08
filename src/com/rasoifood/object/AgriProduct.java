package com.rasoifood.object;

public class AgriProduct {

	private String state;
	private String district;
	private String market;
	private String commodity;
	private String arrivalDate;
	private int price;

	public String toString() {
		return this.getState() + "::" + this.getDistrict() + "::"
				+ this.getMarket() + "::" + this.getCommodity() + "::"
				+ this.getPrice() + "::" + this.getArrivalDate();
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public String getCommodity() {
		return commodity;
	}

	public void setCommodity(String commodity) {
		this.commodity = commodity;
	}

	public String getArrivalDate() {
		return arrivalDate;
	}

	public void setArrivalDate(String arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

}
