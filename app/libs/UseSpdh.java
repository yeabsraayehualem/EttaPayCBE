package use_spdh;

import com.neapay.spdh.Functions;
import com.neapay.spdh.SpdhMessage;

public class UseSpdh {

	public static void main(String[] args) {
		Functions.startConnection("localhost", 9009);
		SpdhMessage m = new SpdhMessage();
		m.setMessageType("A");
		m.setTerminalId("ETTA00000001");
		m.setKeyDownloadTags();
		m.unpack(Functions.sendReceive(m.pack()));

		System.out.println("Key download request RS:" + m.getResponseCode());
		m = new SpdhMessage();
		m.setTerminalId("ETTA00000001");
		m.setPurchaseTags();
		m.setMessageType("F");
		m.setTransactionCode("00");
		m.setFlag1("0");
		m.setFlag2("5");
		m.setAmount("999");
		m.setRetailer("ETTA0001");
		m.setSequenceNumber("1001611");
		m.setTrack2Data(";4197140285412606=26046211076756900000?");
		m.setEmvRequestData("02802312407040813B5FB5DB0081C00000074E7A9FDA600000000000023000000001000006011203A00000");
		m.setEmvAdditionalData("020122      0002E068C8A0000000031010");
		m.setEmvSupplementaryData("019F6E04220000009F660436C04000");
		m.unpack(Functions.sendReceive(m.pack()));
		m.unpack(Functions.sendReceive(m.pack()));
		System.out.println("Purchase Transaction RS:" + m.getResponseCode());

	}

}
