package gov.nih.nci.sparql.util;

import gov.nih.nci.sparql.bean.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.codec.binary.Base64;
import org.json.*;


/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2008-2017 NGIS. This software was developed in conjunction
 * with the National Cancer Institute, and so to the extent government
 * employees are co-authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the disclaimer of Article 3,
 *      below. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   2. The end-user documentation included with the redistribution,
 *      if any, must include the following acknowledgment:
 *      "This product includes software developed by NGIS and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "NGIS" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or NGIS
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      NGIS, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *      POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@ngc.com
 *
 */


public class ParserUtils {
    static String FULL_SYN = "FULL_SYN";
    static String DEFINITION = "DEFINITION";
    static String ALT_DEFINITION = "ALT_DEFINITION";

	public ParserUtils() {

	}

    public String getValue(String t) {
		if (t == null) return null;
		Vector v = StringUtils.parseData(t, '|');
		if (v.size() == 0) return null;
		return (String) v.elementAt(v.size()-1);
	}

	public String parseLabel(Vector v) {
		String t = (String) v.elementAt(0);
		return getValue(t);
	}

	public HashMap parseSuperclasses(Vector v) {
		return parse(v, 2, 1, 0);
	}

	public HashMap parseSubclasses(Vector v) {
		return parse(v, 2, 1, 0);
	}

	public HashMap parseProperties(Vector v) {
		return parse(v, 3, 1, 2);
	}

	public HashMap parse(Vector v, int m, int d1, int d2) {
		HashMap hmap = new HashMap();
		if (v == null) return hmap;
		int n = v.size()/m;
		for (int i=0; i<n; i++) {
			int i0 = i*m;
			int i1 = i0+d1;
			int i2 = i0+d2;
			String t1 = (String) v.elementAt(i1);
			String t2 = (String) v.elementAt(i2);
			String key = getValue(t1);
			String value = getValue(t2);
			Vector u = new Vector();
			if (hmap.containsKey(key)) {
				u = (Vector) hmap.get(key);
			}
			if (!u.contains(value)) {
				u.add(value);
				u = new SortUtils().quickSort(u);
			}
			hmap.put(key, u);
		}
        return hmap;
	}


    public Vector sortSynonyms(Vector v) {
		Vector w = new Vector();
		int n = v.size()/6;
		Vector key_vec = new Vector();
		for (int i=0; i<n; i++) {
			String t = (String) v.elementAt(i*6);
			if (!key_vec.contains(t)) {
				key_vec.add(t);
			}
		}

		key_vec = new SortUtils().quickSort(key_vec);
		for (int i=0; i<key_vec.size(); i++) {
		    String key = (String) key_vec.elementAt(i);
		    for (int j=0; j<v.size(); j++) {
				String value = (String) v.elementAt(j);
				if (value.compareTo(key) == 0) {
					w.add(value);
					w.add((String) v.elementAt(j+1));
					w.add((String) v.elementAt(j+2));
					w.add((String) v.elementAt(j+3));
					w.add((String) v.elementAt(j+4));
					w.add((String) v.elementAt(j+5));
				}
			}
		}
		return w;
	}

/*
       (1) z_axiom|bnode|bnode_8aeefc2e_62d6_4c80_af01_d3f8c0650595_1247066
       (2) x_label|literal|Spinal Cord
       (3) x_code|literal|C12464
       (4) term_name|literal|Medulla Spinalis
       (5) y_label|literal|term-group
       (6) z|literal|SY
*/
	public List getSynonyms(Vector v) {
	    v = sortSynonyms(v);
		List syn_list = new ArrayList();
        String z_axiom = null;
		String code = null;
		String label = null;
		String termName = null;
		String termGroup = null;
		String termSource = null;
		String sourceCode = null;
		String subSourceName = null;
		String subSourceCode = null;

        String prev_z_axiom = null;
		String prev_code = null;
		String prev_label = null;
		String prev_termName = null;
		String prev_termGroup = null;
		String prev_termSource = null;
		String prev_sourceCode = null;
		String prev_subSourceName = null;
		String prev_subSourceCode = null;

		String qualifier_name = null;
		String qualifier_value = null;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String t0 = (String) u.elementAt(0);

			if (t0.compareTo("z_axiom") == 0) {
				z_axiom = (String) u.elementAt(u.size()-1);
				if (prev_z_axiom != null && prev_z_axiom.compareTo(z_axiom) != 0) {
					Synonym syn = new Synonym(
						code,
						label,
						termName,
						termGroup,
						termSource,
						sourceCode,
						subSourceName,
						subSourceCode);
					syn_list.add(syn);

					//System.out.println(syn.toString());

					prev_code = null;
					prev_label = null;
					prev_termName = null;

					termGroup = null;
					termSource = null;
					sourceCode = null;
					subSourceName = null;
					subSourceCode = null;
				}
				prev_z_axiom = z_axiom;

			} else if (t0.compareTo("x_label") == 0) {
				label = getValue(line);
			} else if (t0.compareTo("x_code") == 0) {
				code = getValue(line);
			} else if (t0.compareTo("term_name") == 0) {
				termName = getValue(line);
			} else if (t0.compareTo("y_label") == 0) {
				qualifier_name = getValue(line);
			} else if (t0.compareTo("z") == 0) {
				qualifier_value = getValue(line);
				if (qualifier_name.compareTo("term-source") == 0) {
					termSource = qualifier_value;
				} else if (qualifier_name.compareTo("P385") == 0) {
					sourceCode = qualifier_value;
				} else if (qualifier_name.compareTo("term-group") == 0) {
					termGroup = qualifier_value;
				} else if (qualifier_name.compareTo("source-code") == 0) {
					sourceCode = qualifier_value;
				} else if (qualifier_name.compareTo("subsource-name") == 0) {
					subSourceName = qualifier_value;
				} else if (qualifier_name.compareTo("subsource-code") == 0) {
					subSourceCode = qualifier_value;
				}
			}
		}
		Synonym syn = new Synonym(
			code,
			label,
			termName,
			termGroup,
			termSource,
			sourceCode,
			subSourceName,
			subSourceCode);
		syn_list.add(syn);
		return syn_list;
	}

	public Vector filterPropertyQualifiers(Vector v, String type) { //FULL_SYN, DEFINITION, ALT_DEFINITION
		Vector w = new Vector();
		int n = v.size()/9;
		Vector key_vec = new Vector();
		for (int i=0; i<n; i++) {
			String t0 = (String) v.elementAt(i*9);
			String t3 = (String) v.elementAt(i*9+3);
			t3 = getValue(t3);
			if (t3.compareTo(type) == 0) {
				if (!key_vec.contains(t0)) {
					key_vec.add(t0);
				}
			}
		}
		key_vec = new SortUtils().quickSort(key_vec);
		for (int i=0; i<key_vec.size(); i++) {
		    String key = (String) key_vec.elementAt(i);
		    for (int j=0; j<v.size(); j++) {
				String value = (String) v.elementAt(j);
				if (value.compareTo(key) == 0) {
					w.add(value);
					w.add((String) v.elementAt(j+1));
					w.add((String) v.elementAt(j+2));
					w.add((String) v.elementAt(j+5));
					w.add((String) v.elementAt(j+6));
					w.add((String) v.elementAt(j+8));
				}
			}
		}
		return w;
	}

	public static void main(String[] args) {
		String filename = args[0];

		Vector w = Utils.readFile(filename);
		HashMap hmap = new ParserUtils().parseProperties(w);
		StringUtils.outputHashMap(hmap);

        w = Utils.readFile(filename);
		List list = new ParserUtils().getSynonyms(w);
		for (int i=0; i<list.size(); i++) {
			Synonym syn = (Synonym) list.get(i);
			int j = i+1;
			System.out.println("(" + j + ") " + syn.toString());
		}

		w = Utils.readFile(filename);
		hmap = new ParserUtils().parseSuperclasses(w);
		StringUtils.outputHashMap(hmap);
	}
}
