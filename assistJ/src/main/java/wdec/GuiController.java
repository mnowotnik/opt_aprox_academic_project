package wdec;

import java.net.URL;
import java.util.ResourceBundle;

import opt.Constraints;
import opt.Decision;
import opt.Solver;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class GuiController implements Initializable
{

	@FXML
	private Button calculateButton;

	@FXML
	private TextField cashTextField;

	@FXML
	private TextField debtTextField;

	@FXML
	private TextField periodTextField;

	@FXML
	private TextField volumeTextField;

	@FXML
	private TextField qualityTextField;

	@FXML
	private TextField tvTextField;

	@FXML
	private TextField internetTextField;

	@FXML
	private TextField magazinesTextField;

	@FXML
	private TextField priceTextField;

	@FXML
	private TextField loanTextField;

	@FXML
	private TextField instalmentTextField;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		calculateButton.setOnAction(new EventHandler<ActionEvent>()
		{

			@Override
			public void handle(ActionEvent event)
			{
				Solver solver = new Solver();
				int debt = Integer.parseInt(debtTextField.getText());
				int period = Integer.parseInt(periodTextField.getText());
				int cash = Integer.parseInt(cashTextField.getText());
				Constraints constraints = new Constraints(debt, period, cash);

				Decision decision = solver.solve(constraints);
				
				System.out.println("Decision");
				System.out.println("- volume:" + decision.inputArgs.volume);
				System.out.println("- quality:" + decision.inputArgs.quality);
				System.out.println("- tv:" + decision.inputArgs.ads.tv);
				System.out.println("- internet:" + decision.inputArgs.ads.internet);
				System.out.println("- warehouse:" + decision.inputArgs.ads.warehouse);
				System.out.println("- price:" + decision.inputArgs.price);
				System.out.println("- loan:" + decision.inputArgs.loan);
				System.out.println("- instalment:" + decision.inputArgs.instalment);
				
				volumeTextField.setText(Integer.toString(decision.inputArgs.volume));
				qualityTextField.setText(Integer.toString(decision.inputArgs.quality));
				tvTextField.setText(Double.toString(decision.inputArgs.ads.tv));
				internetTextField.setText(Double.toString(decision.inputArgs.ads.internet));
				magazinesTextField.setText(Double.toString(decision.inputArgs.ads.warehouse));
				priceTextField.setText(Integer.toString(decision.inputArgs.price));
				loanTextField.setText(Integer.toString(decision.inputArgs.loan));
				instalmentTextField.setText(Integer.toString(decision.inputArgs.instalment));
			}

		});

	}

}