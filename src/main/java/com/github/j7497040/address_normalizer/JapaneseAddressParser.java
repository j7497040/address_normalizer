package com.github.j7497040.address_normalizer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;

import lombok.Data;

public class JapaneseAddressParser {

	public static void main(String[] args) {

		JapaneseAddressParser japaneseAddressParser = new JapaneseAddressParser(); 
		japaneseAddressParser.loadJapaneseAddress();
		
	}
	
	
	/**
	 * 日本語住所のリスト.
	 * 都道府県-市区町村-(京都通り名前)-町域 の最小2階層(都道府県-市区町村)、最大4階層のツリー構造.
	 */
	protected static Map<String, Map<String, Map<String, Map<String, String>>>> mapJapaneseAddress = new HashMap<String, Map<String, Map<String, Map<String, String>>>>();
	
	
	/**
	 * 日本語住所の辞書.
	 * 市区町村-都道府県のリスト構造.
	 * 同名市区町村の場合はListが複数になります.
	 */
	Map<String, List<String>> mapCityPrefecture = new HashMap<String, List<String>>();
	
	/**
	 * 都道府県を切り出すためのパターン.
	 */
	protected static final String PREFECTURE_SPLITTER = "([^\\x00-\\x7F]{2,3}県|..府|東京都|北海道)(.+)";
	protected static final Pattern PREFECTURE_PATTERN= Pattern.compile(PREFECTURE_SPLITTER);
	
	/**
	 * 市区町村を切り出すためのパターン.
	 */
	protected static final String CITY_SPLITTER = "^((?:旭川|伊達|石狩|盛岡|奥州|田村|南相馬|那須塩原|東村山|武蔵村山|羽村|十日町|上越|富山|野々市|大町|蒲郡|四日市|姫路|大和郡山|廿日市|下松|岩国|田川|大村)市|.+?郡(?:玉村|大町|.+?)[町村]|.+?市.+?区|.+?[市区町村])(.+)";
	protected static final Pattern CITY_PATTERN= Pattern.compile(CITY_SPLITTER);

	/**
	 * 漢数字
	 */
	protected static final String KANJI_NUM = "[一二三四五六七八九十百千万]";

	/**
	 * 繋ぎ文字1：数字と数字の間(末尾以外)
	 */
	protected static final String BRIDGE_STR = "(丁目|丁|番地|番|号|-|‐|ー|−|の|東|西|南|北)";

	/**
	 * 繋ぎ文字2：数字と数字の間(末尾)
	 */
	protected static final String BRIDGE_LAST_STR = "(丁目|丁|番地|番|号)";

	/**
	 * 全ての数字
	 */
	protected static final String ALL_NUM = "(\\d+|[一二三四五六七八九十百千万]+)";

	/**
	 * 「先頭は数字、途中は数字か繋ぎ文字1、最後は数字か繋ぎ文字2」を満たす正規表現
	 * 町域と丁目、番地、号を切り出すためのパターン.
	 * 丁目、番地、号以前は町域、以降はビル名等になる.
	 */
	protected static final String TOWNAREA_SPLITTER = String.format("%s*(%s|%s{1,2})*(%s|%s)", ALL_NUM, ALL_NUM, BRIDGE_STR, ALL_NUM, BRIDGE_LAST_STR);
	protected static final Pattern TOWNAREA_PATTERN= Pattern.compile(TOWNAREA_SPLITTER);

//	regex6 = /#{all_num}*(#{all_num}|#{s_str1}{1,2})*(#{all_num}|#{s_str2}{1,2})/


	/**
	 * クラスパス内にあるテキストファイルの内容をひとつの文字列として読み込む。
	 * テキストファイルの文字コードはWindows_31Jのみに対応。
	 *
	 */
	public void loadJapaneseAddress() {

		String filepath = "/zenkoku.csv";

		try(
				InputStream is = this.getClass().getResourceAsStream(filepath);
				Reader r = new InputStreamReader(is, "Windows-31J");
				BufferedReader br = new BufferedReader(r);
				CSVParser parser = CSVParser.parse(br, CSVFormat.RFC4180);
				
				) {
			
//			StopWatch sw = new StopWatch("read zenkkoku.csv");
//			sw.start();
			
			parser.forEach(record -> {
				
				System.out.println(record.get(0));
				mapJapaneseAddress.put(key)
				
				// TODO: 住所データをMapに読み込む
			});
			
//			sw.stop();
//			System.out.println(sw.prettyPrint());
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 住所のクレンジング処理.
	 * 
	 * NFKCノーマライズ
	 * ・全角英数字記号、空白は半角に変換.
	 * ・日本語(半角カナ)は全角に変換.
	 * ・ローマ数字は半角英字に変換.
	 * ・合成文字数字①等は半角数字に変換.
	 * ・合成文字数字㈱等は(株)に変換.
	 * 
	 * 記号削除
	 * アルファベットは大文字変換
	 * 括弧書きの注意等削除
	 * 
	 * @param location
	 * @return
	 */
	public static String cleansing(String location) {
		
		// UTF-8 ノーマライズ
		location = Normalizer.normalize(location, Normalizer.Form.NFKC);
		
		// 記号を削除
		location = StringUtils.replaceAll(location ,"\\.|,|。|、|:|・|\\*|゛|゛|'|_|\\/|\\+", "");
		
		// アルファベットを大文字に変換
		location = location.toUpperCase();

		// 括弧書きの注意等は削除
		location = StringUtils.replaceAll(location ,"【.*?】", "");
		location = StringUtils.replaceAll(location ,"≪.*?≫", "");
		location = StringUtils.replaceAll(location ,"《.*?》", "");
		location = StringUtils.replaceAll(location ,"〔.*?〕", "");
		location = StringUtils.replaceAll(location ,"\\[.*?\\]", "");
		location = StringUtils.replaceAll(location ,"<.*?>", "");
		location = StringUtils.replaceAll(location ,"\\(.*?\\)", "");
		location = StringUtils.replaceAll(location ,"「.*?」", "");
		
		
		// 大字,字,小字を削除
		location = location.replaceAll("字|大字|小字", "");
		// "ケ"を"ヶ"に変換する
		location = location.replaceAll("ケ", "ヶ");
		// "之"と"ノ"を"の"に変換する
		location = location.replaceAll("之|ノ", "の");
		// "通り"を"通"に変換する		
		location = location.replaceAll("通り|通リ", "通");
		// "上ル"を"上る"に変換する
		location = location.replaceAll("上ル", "上る");
		// "下ル"を"下る"に変換する
		location = location.replaceAll("下ル", "下る");
		// 末尾"F"を末尾"階"に変換する
		location = location.replaceAll("F$", "階");

		
		// 最後に空白文字等を削除
		location = StringUtils.replaceAll(location, " ", "");
		
		return location;
	}
	
	public static JapaneseAddress parseJapaneseAddress(String location) {
		JapaneseAddress japaneseAddress = new JapaneseAddress();
		
		
		return japaneseAddress;
	}
	
	private static String normalizePrefecture(String prefecture) {
		// 処理不要か？
		return prefecture;
	}
	
	/**
	 * 市区町村から都道府県を類推する.
	 * 
	 * https://ja.wikipedia.org/wiki/%E5%90%8C%E4%B8%80%E5%90%8D%E7%A7%B0%E3%81%AE%E5%B8%82%E5%8C%BA%E7%94%BA%E6%9D%91%E4%B8%80%E8%A6%A7#%E5%90%8C%E4%B8%80%E5%90%8D%E7%A7%B0%E3%81%AE%E5%B8%82
	 * 
	 * を見ると、同一区、同一郡、同一村は無視してよさそう.
	 * 府中市(東京都、広島県)、伊達市(北海道、福島県)だけ考慮すればよい.
	 * ただ、府中市府中町や、伊達市本町など、町域まで被ると類推不能.
	 * それぞれ、東京都、北海道を優先する.
	 * 
	 * @param city 市区町村
	 * @return 都道府県
	 */
	private static String reasonPrefecture(String city) {
		// 同名市区町村判定
		if(isSameCityName(city)) {
			
		}
		
		return null;
	}
	
	private static boolean isSameCityName(String city) {
		return false;
	}
	
	private static String normalizeCity(String city) {
		// 処理不要か？
		return city;
	}

	private static String normalizeTownarea(String townarea) {
		
		return null;
	}

	private static String normalizeExt(String ext) {
		return null;
	}
}

@Data
class JapaneseAddress {

	protected String address;
	protected String prefecture;
	protected String city;
	protected String townarea;
	protected String ext;

	protected String normalizedAddress;
	protected String normalizedPrefecture;
	protected String normalizedCity;
	protected String normalizedTownarea;
	protected String normalizedExt;
}
