package cs572_HW2;

import java.util.Scanner;

public class DemoUI {

	public static void main(String[] args) {
		XMLReader reader = new XMLReader();
		reader.readXML("src/cs572_HW2/config.xml");
		BoostStruct bs = new BoostStruct(reader.getIndexPath());
		System.out.println("indexPath: " + reader.getIndexPath());
		//System.out.println("solrURL: " + reader.getSolrURL());
		//System.out.println("outputPath: " + reader.getOutputPath());
		System.out.println("You can modify the paths in config.xml");
		System.out.println("=======================================================");
		Scanner scan = new Scanner(System.in);
		String input = "";
		while (true) {
			do {
				System.out.println("1. Query");
				System.out.println("2. Content-based Alg");
				System.out.println("3. Link-based Alg");
				System.out.println("4. Enable all Boosts (Update Index)");
				System.out.println("5. Clear all Boosts (Restore Index)");
				System.out.println("6. Quit");
				input = scan.nextLine();
			} while (input.length() < 1);
			char choice = input.charAt(0);
			switch (choice) {
			case '1':
				doQuery(scan, reader, bs);
				break;
			case '2':
				doContentBased(scan, reader, bs);
				break;
			case '3':
				doLinkBased(scan, reader, bs);
				break;
			case '4':
				doEnableBoosts(reader,bs);
				break;
			case '5':
				doClearBoosts(reader,bs);
				break;
			case '6':
				scan.close();
				return;
			default:
				continue;
			}
		}

	}

	private static void doClearBoosts(XMLReader reader, BoostStruct bs) {
		System.out.println("Start Restoring Index..");
		IndexUpdate.restore(reader.getIndexPath(),bs);
		System.out.println("Restoring Finished.");
		System.out.println("");
	}

	private static void doEnableBoosts(XMLReader reader, BoostStruct bs) {
		System.out.println("The previous updating will be overwritten.");
		System.out.println("Start Updating Index..");
		IndexUpdate.update(reader.getIndexPath(),bs);
		System.out.println("Updating Finished.");
		System.out.println("");
	}

	private static void doLinkBased(Scanner scan, XMLReader reader, BoostStruct bs) {
		String input = "";
		LinkBased lb = new LinkBased(reader.getIndexPath());
		while (true) {
			do {
				System.out.println("1. Geographical Relevancy Boost");
				System.out.println("2. Time Relevancy Boost");
				System.out.println("3. Gun Type Relevancy Boost");
				System.out.println("4. Back");
				input = scan.nextLine();
			} while (input.length() < 1);
			char choice = input.charAt(0);
			switch (choice) {
			case '1':
				lb.geoBoost(bs);
				System.out.println("Geographical Relevancy Boost Done.");
				System.out.println("You need to select 4 in the top menu to update the index.");
				System.out.println("");
				break;
			case '2':
				lb.timeBoost(bs);
				System.out.println("Time Relevancy Boost Done.");
				System.out.println("You need to select 4 in the top menu to update the index.");
				System.out.println("");
				break;
			case '3':
				lb.gunTypeBoost(bs);
				System.out.println("Gun Type Relevancy Boost Done.");
				System.out.println("You need to select 4 in the top menu to update the index.");
				System.out.println("");
				break;
			case '4':
				lb.close();
				return;
			default:
				continue;
			}

		}

	}

	private static void doContentBased(Scanner scan, XMLReader reader, BoostStruct bs) {
		String input = "";
		ContentBased cb = new ContentBased(reader.getIndexPath());
		while (true) {
			do {
				System.out.println("1. Unary Term Boost");
				System.out.println("2. TF-IDF Term Boost");
				System.out.println("3. Back");
				input = scan.nextLine();
			} while (input.length() < 1);
			char choice = input.charAt(0);
			switch (choice) {
			case '1':
				do{
					System.out.println("=================================================");
					System.out.println("Boost all the documents which contain the given term.");
					System.out.println("Please enter the term you want to boost.");
					System.out.println("Enter -back to go back.");
					System.out.println("=================================================");
					input = scan.nextLine();
				} while (input.length() < 1);
				if (input.equals("-back"))
					break;
				cb.unaryTermBoost(input, bs);
				System.out.println("Unary Term Boost Done.");
				System.out.println("You need to select 4 in the top menu to update the index.");
				System.out.println("");
				break;
			case '2':
				do{
					System.out.println("=================================================");
					System.out.println("Boost all the documents which contain the given term.");
					System.out.println("Using TF-IDF Algorithm.");
					System.out.println("Please enter the term you want to boost.");
					System.out.println("Enter -back to go back.");
					System.out.println("=================================================");
					input = scan.nextLine();
				} while (input.length() < 1);
				if (input.equals("-back"))
					break;
				cb.tfidfTermBoost(input, bs);
				System.out.println("TF-IDF Term Boost Done.");
				System.out.println("You need to select 4 in the top menu to update the index.");
				System.out.println("");
				break;
			case '3':
				cb.close();
				return;
			default:
				continue;
			}
		}

	}

	private static void doQuery(Scanner scan, XMLReader reader, BoostStruct bs) {
		String input = "";
		MyQuery mq = new MyQuery(reader.getIndexPath());
		while (true) {
			do {
				System.out.println("================================");
				System.out.println("Please enter query string.");
				System.out.println("Enter -back to go back to the top.");
				System.out.println("================================");
				input = scan.nextLine();
			} while (input.length() < 1);
			if (input.equals("-back"))
				break;
			if (bs.isBoosted()) {
				mq.doBoostedQuery(input);
			} else {
				mq.doBaseQuery(input);
			}
		}
		mq.close();
	}

}
