package com.thatoneaiguy;

import com.thatoneaiguy.init.ParoxysmItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Paroxysm implements ModInitializer {
	public static final String MODID = "paroxysm";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		ParoxysmItems.registerAll();
	}
}