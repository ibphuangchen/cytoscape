package wi.bioc.blastpathway;

/**
 * <p>Title: pathblast</p>
 * <p>Description: pathblast</p>
 * <p>Copyright: Copyright (c) 2002 -- 2006 </p>
 * <p>Company: Whitehead Institute</p>
 * <p>Company: University of California, San Diego</p>
 * @author Bingbing Yuan
 * @author Michael Smoot
 * @version 1.2
 */

import java.io.*;
import java.util.*;

import nct.service.sequences.SequenceDatabase;
import nct.service.synonyms.SynonymMapper;

public class IdValidator {

	private Protein p;
	private SequenceDatabase db;
	private SynonymMapper syn;

	public IdValidator(Protein protein, SequenceDatabase db, SynonymMapper syn) {
		p = protein;
		this.db = db;
		this.syn = syn;

	}

	public List<String> validate() { 
		String id = p.getProteinId();
		List<String> validIds = new ArrayList<String>();
		if ( db.contains( id ) ) 
			validIds.add(id);
		else {
			List<String> potSyns = syn.getPotentialSynonyms(id);
			for (String pot : potSyns ) {
				if ( db.contains(pot) ) 
					validIds.add(pot);
			}
		}
		return validIds;
	}
}
