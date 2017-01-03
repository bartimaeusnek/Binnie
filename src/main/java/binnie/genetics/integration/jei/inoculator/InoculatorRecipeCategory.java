package binnie.genetics.integration.jei.inoculator;

import binnie.core.genetics.Gene;
import binnie.genetics.integration.jei.GeneticsJeiPlugin;
import binnie.genetics.integration.jei.RecipeUids;
import binnie.genetics.item.ItemSerum;
import binnie.genetics.machine.inoculator.Inoculator;
import binnie.genetics.machine.inoculator.InoculatorLogic;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAlleleSpecies;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ISpeciesRoot;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class InoculatorRecipeCategory extends BlankRecipeCategory<InoculatorRecipeWrapper> {
	private final IDrawableAnimated arrowAnimated;

	public InoculatorRecipeCategory() {
		this.arrowAnimated = GeneticsJeiPlugin.drawables.createArrowAnimated(56);
	}

	@Override
	public String getUid() {
		return RecipeUids.INOCULATOR;
	}

	@Override
	public String getTitle() {
		return "Inoculation";
	}

	@Override
	public IDrawable getBackground() {
		return GeneticsJeiPlugin.guiHelper.createBlankDrawable(130, 60);
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		IDrawable tank = GeneticsJeiPlugin.drawables.getTank();
		tank.draw(minecraft, 0, 0);

		IDrawable arrow = GeneticsJeiPlugin.drawables.getArrow();
		arrow.draw(minecraft, 69, 25);
		arrowAnimated.draw(minecraft, 69, 25);

		IDrawable slot = GeneticsJeiPlugin.guiHelper.getSlotDrawable();
		slot.draw(minecraft, 22, 0);
		slot.draw(minecraft, 42, 21);

		slot.draw(minecraft, 92, 21);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, InoculatorRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IDrawable tankOverlay = GeneticsJeiPlugin.drawables.getTankOverlay();
		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
		fluidStacks.init(Inoculator.TANK_VEKTOR, true, 1, 1, 16, 58, 100, false, tankOverlay);
		fluidStacks.set(ingredients);

		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 22, 0);
		itemStacks.init(1, true, 42, 21);
		itemStacks.init(2, false, 92, 21);

		IFocus<?> focus = recipeLayout.getFocus();
		Object focusValue = focus.getValue();
		if (focusValue instanceof ItemStack) {
			ItemStack focusStack = (ItemStack) focusValue;
			if (AlleleManager.alleleRegistry.isIndividual(focusStack)) {
				if (focus.getMode() == IFocus.Mode.INPUT) {
					ItemStack serum = recipeWrapper.getInputSerum();
					ItemStack output = InoculatorLogic.applySerum(focusStack, serum);
					itemStacks.set(0, serum);
					itemStacks.set(1, focusStack);
					itemStacks.set(2, output);
					return;
				} else if (focus.getMode() == IFocus.Mode.OUTPUT) {
					IIndividual individual = AlleleManager.alleleRegistry.getIndividual(focusStack);
					ISpeciesRoot speciesRoot = individual.getGenome().getSpeciesRoot();
					IAlleleSpecies species = individual.getGenome().getPrimary();
					ItemStack serum = ItemSerum.create(new Gene(species, speciesRoot.getSpeciesChromosomeType(), speciesRoot));
					serum.setItemDamage(0); // set fully charged

					itemStacks.set(0, serum);
					itemStacks.set(1, recipeWrapper.getWildcardTarget());
					itemStacks.set(2, focusStack);
					return;
				}
			} else if (focusStack.getItem() instanceof ItemSerum) {
				ItemStack input = recipeWrapper.getWildcardTarget();
				ItemStack output = InoculatorLogic.applySerum(input, focusStack);
				itemStacks.set(0, focusStack);
				itemStacks.set(1, input);
				itemStacks.set(2, output);
				return;
			}
		}

		itemStacks.set(ingredients);
	}
}
