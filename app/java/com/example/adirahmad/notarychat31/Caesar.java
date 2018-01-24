/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.adirahmad.notarychat31;

/**
 *
 * @author AdiRahmad
 */
public class Caesar {
    
    public String enkripsiCaesar(String text) {


		int key = 3;
		String terenkripsi = "";

		for (int i = 0; i < text.length(); i++) {

			int c = text.charAt(i);

			if (Character.isUpperCase(c)) {
				c = c + (key % 26);
				if (c > 'Z') {
					c = c - 26;
				}
			} else if (Character.isLowerCase(c)) {
				c = c + (key % 26);
				if (c > 'z') {
					c = c - 26;
				}
			}
			terenkripsi += (char) c;
		}

		return terenkripsi;
	}

	public String dekripsiCaesar(String text) {


		int key = 3;

		String terdekripsi = "";


		for (int i = 0; i < text.length(); i++) {

			int c = text.charAt(i);


			if (Character.isUpperCase(c)) {
				
				c = c - (key % 26);

				if (c < 'A') {
				
					c = c + 26;
				
				}
			} else if (Character.isLowerCase(c)) {
			
				c = c - (key % 26);

				if (c < 'a') {
		
					c = c + 26;
		
				}
			}
			terdekripsi += (char) c;
		}
	

		return terdekripsi;
	}

    
}
