package wdec;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import opt.Decision;
import opt.Solver;

public class GuiController implements CalcInterface {

	CalcInterface afterCalc = this;
	double glowLevel;
	boolean glowUp;
	List<Decision> decisions;

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

	@FXML
	private TextField unitPriceTextField;

	@FXML
	private TextField grossSalesIncomeTextField;

	@FXML
	private TextField primeCostsTextField;

	@FXML
	private TextField salesIncomeTextField;

	@FXML
	private TextField realNetIncomeTextField;

	@FXML
	private TextField wdecNetIncomeTextField;

	@FXML
	private TextField riskTextField;
	
	@FXML
	private Text riskText;

	@FXML
	private Pane calculatingInfo;

	@FXML
	private LineChart<Number, Number> lineChart;
	
	@FXML
	private NumberAxis xAxis, yAxis;

	@FXML
	public void initialize() {
		lineChart.setLegendVisible(false);
		StringConverter<Number> stringFormatter = new StringConverter<Number>() {

		     @Override 
		     public String toString(Number number) {
				return (new Double(number.doubleValue()*100)).toString() + '%';
		     }

			@Override
			public Number fromString(String arg0)
			{
				// We don't need this, do we?
				return null;
			}
		 };
		
		xAxis.setTickLabelFormatter(stringFormatter);

		debtTextField.setOnKeyPressed(null);

		EventHandler<ActionEvent> execCalcEv = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				try {
					int debt = Integer.parseInt(debtTextField.getText());
					int period = Integer.parseInt(periodTextField.getText());
					int cash = Integer.parseInt(cashTextField.getText());
					new CalculatingThread(debt, period, cash, afterCalc);
					calculatingInfo.setVisible(true);
					calculateButton.setDisable(true);
					glowLevel = 0.0;
					calculatingInfo.setEffect(new Glow(0.0));
					glowUp = true;
				} catch (NumberFormatException e) {
					System.out.println("Wrong format");
					return;
				}
			}

		};

		calculateButton.setOnAction(execCalcEv);

		TextField[] inputFields = new TextField[] { debtTextField,
				periodTextField, cashTextField };
		EventHandler<KeyEvent> enterExecEv = new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) {
					for (TextField tf : inputFields) {
						if (tf.getText().equals("")) {
							return;
						}

					}

					execCalcEv.handle(new ActionEvent());
				}
			}
		};

		for (TextField tf : inputFields) {
			tf.setOnKeyPressed(enterExecEv);
		}
		
		riskText.setText("0%");
	}

	@Override
	public void fillTextFields(Decision decision) {
		volumeTextField.setText(Integer.toString(decision.inputArgs.volume));
		qualityTextField.setText(Integer.toString(decision.inputArgs.quality));
		tvTextField.setText(toIntFormat(decision.inputArgs.ads.tv));
		internetTextField.setText(toIntFormat(decision.inputArgs.ads.internet));
		magazinesTextField.setText(toIntFormat(decision.inputArgs.ads.warehouse));
		priceTextField.setText(Integer.toString(decision.inputArgs.price));
		loanTextField.setText(Integer.toString(decision.inputArgs.loan));
		instalmentTextField.setText(Integer
				.toString(decision.inputArgs.instalment));

		double formattedUnitPrice = new BigDecimal(
				decision.parameters.unitPrice).setScale(2,
				BigDecimal.ROUND_HALF_UP).doubleValue();
		unitPriceTextField.setText(Double.toString(formattedUnitPrice));
		grossSalesIncomeTextField
				.setText(toIntFormat(decision.report.grossSalesIncome));
		primeCostsTextField.setText(toIntFormat(decision.report.primeCosts));
		salesIncomeTextField.setText(toIntFormat(decision.report.salesIncome));

		double risk = 1.0 - decision.objectives.percSold;
		BigDecimal formattedRisk = new BigDecimal(risk * 100).setScale(5,
				BigDecimal.ROUND_HALF_UP);

		realNetIncomeTextField.setText(toIntFormat(decision.objectives.netIncome));

		double wdecIncome = Solver.convertToWdecIncome(
				decision.objectives.netIncome, decision.inputArgs.instalment);
		wdecNetIncomeTextField.setText(toIntFormat(Math.round(wdecIncome)));

		riskText.setText(formattedRisk.toString() + '%');
	}

	private String toIntFormat(double d) {
		return String.format("%.0f", d);
	}

	@Override
	public void calcGlow() {
		if (glowUp) {
			glowLevel = glowLevel + 0.01;
			if (glowLevel >= 1.0) {
				glowUp = false;
			}
		} else {
			glowLevel = glowLevel - 0.01;
			if (glowLevel <= 0.0)
				glowUp = true;
		}
		calculatingInfo.setEffect(new Glow(glowLevel));
	}

	@Override
	public void addDecisions(List<Decision> decisions) {

		lineChart.getData().clear();

		lineChart.getXAxis().setAutoRanging(true);
		lineChart.getYAxis().setAutoRanging(true);

		XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();

		List<Node> nodeList = new ArrayList<Node>();
		List<Decision> filteredDecisionList = new ArrayList<Decision>();
		int intTemp = 0;
		
		for (Decision decision : decisions) {
			double income = decision.objectives.netIncome;
			double risk = 1.0 - decision.objectives.percSold;

//			if(risk <= 0.001)
//			{
				XYChart.Data<Number, Number> dataTemp = new XYChart.Data<Number, Number>(risk, income);
				series1.getData().add(dataTemp);
				intTemp++;
				filteredDecisionList.add(decision);
//			}
		}
		
		lineChart.getData().add(series1);

		for (int i = 0; i < intTemp; i++) {
			nodeList.add(lineChart.getData().get(0).getData().get(i).getNode());
		}

		setOnMouseEventsOnSeries(nodeList, filteredDecisionList);

		calculatingInfo.setVisible(false);
		calculateButton.setDisable(false);
		};

	private void setOnMouseEventsOnSeries(List<Node> nodeList,
			List<Decision> filteredDecisionList) {
		for (int i = 0; i < nodeList.size(); i++) {
			final int temp = i;
			nodeList.get(i).setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent t) {

					fillTextFields(filteredDecisionList.get(temp));

				}
			});
		}

	}

}