package opt;

public class InputArgs {

	public final double volume, quality, price, loan, instalment;

	public InputArgs(double volume, double quality, double price, double loan,
			double instalment, Advertisments ads) {
		super();
		this.volume = volume;
		this.quality = quality;
		this.price = price;
		this.loan = loan;
		this.instalment = instalment;
		this.ads = ads;
	}

	public final Advertisments ads;
}
