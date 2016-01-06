package com.payment;

import java.util.Arrays;
import java.util.List;

import com.payment.util.Item;

public class ConfigIAP {
	/**
	 * GOOGLE_IAP_APP_KEY encrypt  then save
	 * here. decode it later in googlePlayiapMangaer
	 */
	public static final String base64EncodedPublicKey = "72C42B8224AB2D7E4BE6F371FE36C913B35B59C7D704E2AEDD46A25A21F260AA99570099F7A7A4EECCDC9CF0665CAA6007A6F24297C7E66463BF224284637DC66B8CAA680D943135EBF6764275F534C5E2A85B2D2D7A0143954B10D44A8AA9F7C49B0649F61080F515A1CAC70EE64C7B1653DE044FEF685B039C9F3F88DACDECC74C6F4A1C698CB032F77523DEB653C29965763646630A378B72E24078EEFC19D4E33F317DE712B99DD7B329DFD645B9FF8EFF2D490C7A919B9C8198142B7B35465E44B7F646D5D6E6FDF61F8418967FBF3D4E0420FE7D510C810980123859DE779EDEE05E66DE4E3D3D691019E7CC12041DA6C52DA5D474C436E5D72467773311AAF58EF0311F2F5D2D03067942D1343381E80B4A7C6D03AE46DC6ED89BF380A53CC48DDBD4426E7475FC8A536D0D8BFA86C10A5C0B2971858BC0F24EBCA13E3CDB0F9ACC2F0C36DD052814B98B978E9CFE7A17A2C4D840671611EC5ACBFCE50B24C0B52D5792FD9B6B52E26549D2C70FC3A3797D17A8ED8C5DD448314B52188D0F44FDD3CB897F2296CD1173742618";

	// public static final String base64EncodedPublicKey =
	// "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp3ufgYwOzneNQKr7EvR0zfUK+IvcaLtMOAofEhCY4JMaDcrAffUcxtJz7xRkbZgPq795rWb5EdfvvA0JkPy/wyE5kRxqwVXvbSsBjfaVoXkQ3rT/Gb2rhvv9CVAYG4T63ZEbd5Ymryj/++DAF+MovaEKzYE2sVAOXiS2hicqBhV6Jz1CZ4w5k278vEB522H9sR2WSqkDO+Qdi2R2KAw8ej+c/FwBRbXAn401KIeVY1gCgrB2Z6pQMYu5mwfYU4yOjGZiV39g9KMBP5bQ+yIBPv/cY7pGsqTjgH7i/qY3+FS5bFf/e6XmF9Ac4THHGjA6BqG8/pbC3egRjaaDvZdDJQIDAQAB";//
	public static final String SKU_100POINT = "jp.sugutomo.point.100";
	public static final String SKU_200POINT = "jp.sugutomo.point.200";
	public static final String SKU_500POINT = "jp.sugutomo.point.500";
	public static final String SKU_700POINT = "jp.sugutomo.point.700";
	public static final String SKU_1000POINT = "jp.sugutomo.point.1000";
	public static final String SKU_1500POINT = "jp.sugutomo.point.1500";
	public static final String SKU_2000POINT = "jp.sugutomo.point.2000";
	public static final String SKU_3000POINT = "jp.sugutomo.point.3000";
	public static final String SKU_5000POINT = "jp.sugutomo.point.5000";
	public static final String SKU_6000POINT = "jp.sugutomo.point.6000";
	public static final String SKU_8000POINT = "jp.sugutomo.point.8000";
	public static final String SKU_10000POINT = "jp.sugutomo.point.10000";
	// public static List<String> consumeItems =
	// Arrays.asList("android.test.purchased","android.test.canceled","android.test.refunded");
	public static List<String> consumeItems = Arrays.asList(SKU_100POINT,
			SKU_200POINT, SKU_500POINT, SKU_700POINT, SKU_1000POINT,
			SKU_1500POINT, SKU_2000POINT, SKU_3000POINT, SKU_5000POINT,
			SKU_6000POINT, SKU_8000POINT, SKU_10000POINT);
	public static List<Item> consumeItemsPrice = Arrays.asList(new Item(100,
			100), new Item(200, 200), new Item(500, 500), new Item(700, 700),
			new Item(1000, 1000), new Item(1500, 1500), new Item(2000, 2000),
			new Item(3000, 3000), new Item(5000, 5000), new Item(6000, 6000),
			new Item(8000, 8000), new Item(10000, 10000));

}
