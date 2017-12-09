package de.alphahelix.alphalibary.forms.d3;

import de.alphahelix.alphalibary.forms.Form;
import de.alphahelix.alphalibary.forms.d2.RectangleForm;
import org.bukkit.entity.Player;

@SuppressWarnings("ALL")
public class CuboidForm extends Form {

    private RectangleForm rectangleForm;
    private double width;

    public CuboidForm(RectangleForm rectangleForm, double width) {
        super(rectangleForm.getLocation(), rectangleForm.getAxis(), rectangleForm.getDense(), rectangleForm.getAction());
        this.width = width;
        this.rectangleForm = rectangleForm;
    }

    public RectangleForm getRectangleForm() {
        return rectangleForm;
    }

    public CuboidForm setRectangleForm(RectangleForm rectangleForm) {
        this.rectangleForm = rectangleForm;
        return this;
    }

    public double getWidth() {
        return width;
    }

    public CuboidForm setWidth(double width) {
        this.width = width;
        return this;
    }

    @Override
    public void send(Player p) {
        new RectangleForm(getLocation(), getAxis(), getDense(), getRectangleForm().getLenght(), getRectangleForm().getHeight(), getRectangleForm().isFilled(), getAction()).send(p);
        new RectangleForm(getLocation().clone().add(0, 0, getWidth()), getAxis(), getDense(), getRectangleForm().getLenght(), getRectangleForm().getHeight(), getRectangleForm().isFilled(), getAction()).send(p);

        for (double w = getDense(); w < (getWidth() - getDense()); w += getDense()) {
            new RectangleForm(getLocation().clone().add(0, 0, w), getAxis(), getDense(), getRectangleForm().getLenght(), getRectangleForm().getHeight(), false, getAction()).send(p);
        }
    }
}