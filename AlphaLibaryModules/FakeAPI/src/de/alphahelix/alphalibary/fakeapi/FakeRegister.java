/*
 * Copyright (C) <2017>  <AlphaHelixDev>
 *
 *       This program is free software: you can redistribute it under the
 *       terms of the GNU General Public License as published by
 *       the Free Software Foundation, either version 3 of the License.
 *
 *       This program is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU General Public License for more details.
 *
 *       You should have received a copy of the GNU General Public License
 *       along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.alphahelix.alphalibary.fakeapi;

import de.alphahelix.alphalibary.fakeapi.files.*;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeRegister {
	
	private static ArmorstandLocationsFile armorstandLocationsFile;
	private static EndercrystalLocationsFile endercrystalLocationsFile;
	private static PlayerLocationsFile playerLocationsFile;
	private static ItemLocationsFile itemLocationsFile;
	private static MobLocationsFile mobLocationsFile;
	private static BigItemLocationsFile bigItemLocationsFile;
	private static XPOrbLocationsFile xpOrbLocationsFile;
	
	public static ArmorstandLocationsFile getArmorstandLocationsFile() {
		return armorstandLocationsFile;
	}
	
	public static EndercrystalLocationsFile getEndercrystalLocationsFile() {
		return endercrystalLocationsFile;
	}
	
	public static ItemLocationsFile getItemLocationsFile() {
		return itemLocationsFile;
	}
	
	public static PlayerLocationsFile getPlayerLocationsFile() {
		return playerLocationsFile;
	}
	
	public static MobLocationsFile getMobLocationsFile() {
		return mobLocationsFile;
	}
	
	public static BigItemLocationsFile getBigItemLocationsFile() {
		return bigItemLocationsFile;
	}
	
	public static XPOrbLocationsFile getXpOrbLocationsFile() {
		return xpOrbLocationsFile;
	}
	
	
	void initAll(JavaPlugin plugin) {
		armorstandLocationsFile = new ArmorstandLocationsFile(plugin);
		endercrystalLocationsFile = new EndercrystalLocationsFile(plugin);
		playerLocationsFile = new PlayerLocationsFile(plugin);
		itemLocationsFile = new ItemLocationsFile(plugin);
		mobLocationsFile = new MobLocationsFile(plugin);
		bigItemLocationsFile = new BigItemLocationsFile(plugin);
		xpOrbLocationsFile = new XPOrbLocationsFile(plugin);
	}
}
