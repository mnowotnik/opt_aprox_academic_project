package opt;

public class InputArgs {

	public final int volume, quality, price, loan, instalment;

	public InputArgs(int volume, int quality, int price, int loan,
			int instalment, Advertisments ads) {
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
