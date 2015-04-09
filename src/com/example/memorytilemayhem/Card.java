package com.example.memorytilemayhem;

import android.widget.Button;

//This class is used to help compare cards
public class Card{

	public int x;
	public int y;
	public Button button;
	
	public Card(Button button, int x,int y) {
		this.x = x;
		this.y = y;
		this.button = button;
	}
	

}
