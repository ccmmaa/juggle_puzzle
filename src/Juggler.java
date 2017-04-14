///////////////////////////////////////////////////
/* *
 * Programmer: Cristina M. Anderson
 * Date last edited: Jan 3, 2016
 * Notes: needs classes Participant and Circuit to work
 * */
///////////////////////////////////////////////////
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Juggler {
	static ArrayList<Circuit> circuits = new ArrayList<Circuit>();
	static ArrayList<Participant> jugglers = new ArrayList<Participant>();
	public static void main(String [] args) throws FileNotFoundException{
		
		//retrieve data
		Scanner in = new Scanner(new File("jugglefest.txt"));
		while(in.hasNext() && in.next().equals("C")){
			getCircuitData(in);
		}
		do{
			getJugglerData(in);
		} while(in.hasNext() && in.next().equals("J"));
		in.close();
		
		//get scores for matches
		int jug = jugglers.size();
		int cir = circuits.size();
		for(int j = 0; j < jug; j++){
			Participant currentP = jugglers.get(j);
			ArrayList<Integer> pskills = currentP.skills;
			int ski = pskills.size();
			for(int c = 0; c < cir; c++){
				Circuit currentC = circuits.get(c);
				ArrayList<Integer> cskills = currentC.skills;
				int score = 0;
				for(int s = 0; s < ski; s++){
					score += pskills.get(s)*cskills.get(s);
				}
				currentP.scores.add(score);
			}
		}
		
		//get sorted lists for each circuit based on jugglers' first preferences
		int numJugPerCir = jug/cir;
		int[] max = new int[cir];
		Arrays.fill(max, 0);
		for(int i = 0; i < jug; i++){
			Participant currentJ = jugglers.get(i);
			int c = currentJ.preference[0];
			ArrayList<Integer> results = circuits.get(c).results;
			int currentScore = currentJ.scores.get(c);
			if(max[c] < numJugPerCir){
				boolean inserted = false;
				for(int j = 0; j < max[c] && !inserted; j++){
					Participant otherJ = jugglers.get(results.get(j));
					if(otherJ.scores.get(c) > currentScore){
						results.add(j, i);
						currentJ.used = true;
						inserted = true;
						max[c]++;
					}
				}
				if(inserted == false){
					results.add(i);
					max[c]++;
					currentJ.used = true;
				}
			} else{
				boolean inserted = false;
				Participant otherJ = jugglers.get(results.get(0));
				if(otherJ.scores.get(c) < currentScore){
					for(int j = 0; j < max[c] && !inserted; j++){
						otherJ = jugglers.get(results.get(j));
						if(otherJ.scores.get(c) >= currentScore){
							results.add(j, i);
							currentJ.used = true;
							inserted = true;
						}
					}
					if(!inserted){
						results.add(i);
						currentJ.used = true;
					}
					Participant oldJ = jugglers.get(results.get(0));
					oldJ.used = false;
					results.remove(0);
				}
			}
		}
		
		//get sorted lists for each circuit based on jugglers' second preferences
		for(int i = 0; i < jug; i++){
			Participant currentJ = jugglers.get(i);
			if(currentJ.used == false){
				orderJugglers(currentJ, cir, i, numJugPerCir);
			}
		}
		
		//add remaining jugglers that don't fit anywhere based on their preferences into the remaining
		//circuits that do not have enough jugglers(done arbitrarily)
		for(int i = 0; i < cir; i++){
			Circuit currentC = circuits.get(i);
			ArrayList<Integer> results = currentC.results;
			int size = results.size();
			if(size < numJugPerCir){
				for(int j = 0; j < jug && results.size() < numJugPerCir; j++){
					Participant currentJ = jugglers.get(j);
					if(!currentJ.used){
						results.add(j);
						currentJ.used = true;
					}
				}
			}
		}
		
		//print results to text file
		PrintWriter writer = new PrintWriter("juggleroutput.txt");
		for(int i = 0; i < cir; i++){
			Circuit currentC = circuits.get(i);
			writer.print(currentC.name + " ");
			int jugInt = currentC.results.get(0);
			Participant currentJ = jugglers.get(jugInt);
			writer.print(currentJ.name);
			int len = currentJ.preference.length;
			for(int k = 0; k < len; k++){
				int prefC = currentJ.preference[k];
				int scoreC = currentJ.scores.get(prefC);
				writer.print(" C" + prefC + ":" + scoreC);
			}
			for(int j = 1; j < numJugPerCir; j++){
				jugInt = currentC.results.get(j);
				currentJ = jugglers.get(jugInt);
				writer.print(", " + currentJ.name);
				for(int k = 0; k < len; k++){
					int prefC = currentJ.preference[k];
					int scoreC = currentJ.scores.get(prefC);
					writer.print(" C" + prefC + ":" + scoreC);
				}
			}
			writer.println();
		}
		writer.close();
	}

	//puts juggler in circuit based on preference and if juggler knocks out other juggler from circuit
	//uses recursion to put the knocked out juggler in a circuit
	public static void orderJugglers(Participant currentJ, int cir, int i, int numJugPerCir){
		boolean used = false;
		int prefSize = currentJ.preference.length;
		for(int j = currentJ.nextPref; j < prefSize && !used; j++){
			int c = currentJ.preference[j];
			Circuit currentC = circuits.get(c);
			ArrayList<Integer> results = currentC.results;
			int currentScore = currentJ.scores.get(c);
			int size = results.size();
			if(size < numJugPerCir){
				results.add(i);
				currentJ.used = true;
				currentJ.nextPref = j+1;
				used = true;
			} else{
				boolean inserted = false;
				Participant otherJ = jugglers.get(results.get(0));
				if(otherJ.scores.get(c) < currentScore){
					for(int k = 1; k < size && !inserted; k++){
						otherJ = jugglers.get(results.get(k));
						if(otherJ.scores.get(c) >= currentScore){
							results.add(k, i);
							currentJ.used = true;
							currentJ.nextPref = j+1;
							inserted = true;
						}
					}
					if(!inserted){
						results.add(i);
						currentJ.used = true;
						currentJ.nextPref = j+1;
					}
					Participant next = jugglers.get(results.get(0));
					next.used = false;
					orderJugglers(next, cir, results.get(0), numJugPerCir);
					used = true;
					results.remove(0);
				}
			}
		}
	}
	
	public static void getCircuitData(Scanner in){
		Circuit c = new Circuit();
		c.name = in.next();
		c.results = new ArrayList<Integer>();
		c.skills = new ArrayList<Integer>();
		getSkills(in, c.skills);
		circuits.add(c);
		in.nextLine();
	}
	
	public static void getJugglerData(Scanner in){
		Participant p = new Participant();
		p.name = in.next();
		p.skills = new ArrayList<Integer>();
		p.scores = new ArrayList<Integer>();
		p.nextPref = 1;
		getSkills(in, p.skills);
		String pref = in.next();
		String[] aPref = pref.split(",");
		int len = aPref.length;
		int[] iPref = new int[len];
		for(int i = 0; i < len; i++){
			iPref[i] = Integer.parseInt(aPref[i].substring(1));
		}
		p.preference = iPref;
		jugglers.add(p);
		if(in.hasNextLine()){
			in.nextLine();
		}
	}
	
	public static void getSkills(Scanner in, ArrayList<Integer> a){
		for(int i = 0; i < 3; i++){
			String skill = in.next();
			String sNum = skill.substring(2);
			Integer num = Integer.parseInt(sNum);
			a.add(num);
		}
	}
}
