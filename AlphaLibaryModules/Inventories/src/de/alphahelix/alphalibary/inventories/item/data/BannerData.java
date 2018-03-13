package de.alphahelix.alphalibary.inventories.item.data;

import com.google.common.base.Objects;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.Arrays;

public class BannerData implements ItemData {
	
	private final Pattern[] patterns;
	
	public BannerData(Pattern... patterns) {
		this.patterns = patterns;
	}
	
	@Override
	public void applyOn(ItemStack applyOn) {
		if(applyOn.getType() != Material.BANNER) return;
		
		BannerMeta meta = (BannerMeta) applyOn.getItemMeta();
		
		for(Pattern pattern : patterns)
			meta.addPattern(pattern);
		
		applyOn.setItemMeta(meta);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode((Object[]) patterns);
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		BannerData that = (BannerData) o;
		return Objects.equal(patterns, that.patterns);
	}
	
	@Override
	public String toString() {
		return "BannerData{" +
				"patterns=" + Arrays.toString(patterns) +
				'}';
	}
}
