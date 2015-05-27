package wdec;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.neural.networks.BasicNetwork;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import javafx.util.converter.PercentageStringConverter;
import opt.Advertisments;
import opt.Decision;
import opt.ObjLoader;
import opt.PercSoldFun;
import opt.Solver;
import opt.UnitPriceFun;

public class GuiController implements CalcInterface {

	CalcInterface afterCalc = this;
	double glowLevel;
	boolean glowUp;
	List<Decision> decisions;

	@FXML
	private Button calculateButton;

	@FXML
	private TextField cashTextField, debtTextField, periodTextField, volumeTextField, qualityTextField,
					  tvTextField, internetTextField, magazinesTextField, priceTextField, loanTextField,
					  instalmentTextField, unitPriceTextField, grossSalesIncomeTextField, 
					  primeCostsTextField, salesIncomeTextField, realNetIncomeTextField,
					  wdecNetIncomeTextField, riskTextField;

	@FXML
	private Text riskText, qualityText;

	@FXML
	private Pane calculatingInfo, precSoldControlls;

	@FXML
	private LineChart<Number, Number> lineChart, unitPriceChart, precSoldChart;

	@FXML
	private NumberAxis xAxis, yAxis, xAxis2, yAxis2;
	
	@FXML
	private ToggleButton solverToggle, unitPriceToggle, precSoldToggle;

	@FXML
	private Slider unitPriceSlider;
	
	@FXML
	private Circle pricePlaceholder, tvPlaceholder, internetPlaceholder, magazinesPlaceholder;
	
	private Slider priceKnob, tvKnob, internetKnob, magazinesKnob;
	
	@FXML
	public void initialize() {
		lineChart.setLegendVisible(false);
		unitPriceChart.setLegendVisible(false);
		precSoldChart.setLegendVisible(false);
		
		calculateUnitPriceChart((int)unitPriceSlider.getValue());
		
		
		Circle[] circles = new Circle[] {pricePlaceholder,tvPlaceholder,internetPlaceholder,magazinesPlaceholder};
		for(Circle circle: circles)
		{
			circle.setVisible(false);
		}
		
		ChangeListener<Number> knobChange = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0,
					Number arg1, Number arg2)
			{
				calculatePrecSoldChart();
			}
        };
        
		priceKnob = initializeKnob(pricePlaceholder, 80);
		tvKnob = initializeKnob(tvPlaceholder, 500);
		internetKnob = initializeKnob(internetPlaceholder, 500);
		magazinesKnob = initializeKnob(magazinesPlaceholder, 500);
		
		priceKnob.valueProperty().addListener(knobChange);
		tvKnob.valueProperty().addListener(knobChange);
		internetKnob.valueProperty().addListener(knobChange);
		magazinesKnob.valueProperty().addListener(knobChange);
		calculatePrecSoldChart();
        
		
		StringConverter<Number> stringFormatter = new StringConverter<Number>() {

			@Override
			public String toString(Number number) {
				return (new Double(number.doubleValue() * 100)).toString() + '%';
			}

			@Override
			public Number fromString(String arg0) {
				// We don't need this, do we?
				return null;
			}
		};

		xAxis.setTickLabelFormatter(stringFormatter);
		((NumberAxis)precSoldChart.getYAxis()).setTickLabelFormatter(new PercentageStringConverter());

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
		
		EventHandler<ActionEvent> showUnitPriceChart = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				lineChart.setVisible(false);
				solverToggle.setSelected(false);
				
				unitPriceChart.setVisible(true);
				unitPriceSlider.setVisible(true);
				unitPriceToggle.setSelected(true);
				qualityText.setVisible(true);
				
				precSoldChart.setVisible(false);
				precSoldToggle.setSelected(false);
				precSoldControlls.setVisible(false);

				}
		};
		
		unitPriceToggle.setOnAction(showUnitPriceChart);
		
		EventHandler<ActionEvent> showSolverChart = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				lineChart.setVisible(true);
				solverToggle.setSelected(true);
				
				unitPriceChart.setVisible(false);
				unitPriceSlider.setVisible(false);
				unitPriceToggle.setSelected(false);
				qualityText.setVisible(false);
				
				precSoldChart.setVisible(false);
				precSoldToggle.setSelected(false);
				precSoldControlls.setVisible(false);
				
				}
		};
		
		solverToggle.setOnAction(showSolverChart);
		
		EventHandler<ActionEvent> showPrecSoldChart = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				lineChart.setVisible(false);
				solverToggle.setSelected(false);
				
				unitPriceChart.setVisible(false);
				unitPriceSlider.setVisible(false);
				unitPriceToggle.setSelected(false);
				qualityText.setVisible(false);
				
				precSoldChart.setVisible(true);
				precSoldToggle.setSelected(true);
				precSoldControlls.setVisible(true);
				
				}
		};
		
		precSoldToggle.setOnAction(showPrecSoldChart);
		
		ChangeListener<Number> sliderChange = new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0,
						Number arg1, Number arg2)
				{
					calculateUnitPriceChart(arg2.intValue());
					
				}
	        };
		
	    unitPriceSlider.valueProperty().addListener(sliderChange);
	

		TextField[] inputFields = new TextField[] { debtTextField,
				periodTextField, cashTextField };

		for (TextField tf : inputFields) {
			tf.setOnKeyPressed(event -> {
				if (event.getCode().equals(KeyCode.ENTER)) {
					for (TextField tf1 : inputFields) {
						if (tf1.getText().equals("")) {
							return;
						}

					}

					execCalcEv.handle(new ActionEvent());
				}
			});
		}

		riskText.setText("0%");
	}

	@Override
	public void fillTextFields(Decision decision) {
		volumeTextField.setText(Integer.toString(decision.inputArgs.volume));
		qualityTextField.setText(Integer.toString(decision.inputArgs.quality));
		tvTextField.setText(toIntFormat(decision.inputArgs.ads.tv));
		internetTextField.setText(toIntFormat(decision.inputArgs.ads.internet));
		magazinesTextField
				.setText(toIntFormat(decision.inputArgs.ads.warehouse));
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

		realNetIncomeTextField
				.setText(toIntFormat(decision.objectives.netIncome));

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
//		yAxis.invalidateRange(lineChart.getData().get(0).getData()
//				.stream()
//				.map(Data::getYValue)
//				.collect(Collectors.toList()));

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
			nodeList.get(i).setOnMouseClicked(
					ev -> fillTextFields(filteredDecisionList.get(temp)));
		}

	}
	
	private void calculateUnitPriceChart(int quality)
	{
		unitPriceChart.getData().clear();
		
		XYChart.Series<Number,Number> series = new
				XYChart.Series<Number,Number>();
		
		ObjLoader ol = new ObjLoader();
		BasicNetwork upriceNN = ol.loadUPriceNN();
		NormalizationHelper upriceNormHelper = ol.loadNormHelperUprice();
		UnitPriceFun upriceFun = new UnitPriceFun(upriceNormHelper, upriceNN);
		
		for(int i = 0; i < 400;i++)
		{
			double price = upriceFun.compute(i*1000, quality);
			XYChart.Data<Number, Number> dataTemp = new XYChart.Data<Number, Number>(i*1000,price);
			series.getData().add(dataTemp);

		}
		
		unitPriceChart.getData().add(series);
		series.getNode().setStyle("-fx-stroke: LIMEGREEN;");
	}
	
	private Slider initializeKnob(Circle placeholder, int maxValue)
	{
		Slider slider = new Slider(1,maxValue,1);
		slider.setBlockIncrement(0.1);
		//slider.setId("knob");
		slider.getStyleClass().add("knobStyle");
        precSoldControlls.getChildren().add(slider);
        slider.resizeRelocate(placeholder.getLayoutX()-placeholder.getRadius(),
        			placeholder.getLayoutY()-placeholder.getRadius(),
        			placeholder.getRadius(),
        			placeholder.getRadius());
        return slider;
        
	}
	
	protected void calculatePrecSoldChart()
	{
		int price = (int) priceKnob.getValue();
		int tv = (int) tvKnob.getValue()*1000;
		int internet = (int) internetKnob.getValue()*1000;
		int magazines = (int) magazinesKnob.getValue()*1000;
		
		precSoldChart.getData().clear();
		
		XYChart.Series<Number,Number> series = new
				XYChart.Series<Number,Number>();
		
		ObjLoader ol = new ObjLoader();
		BasicNetwork percSoldNN = ol.loadPercSoldNN();
		NormalizationHelper percSoldNormHelper = ol.loadNormHelperPercSold();
		PercSoldFun percSoldFun = new PercSoldFun(percSoldNormHelper, percSoldNN);
		
		for(int i = 0; i < 100;i++)
		{
			double precSold = percSoldFun.compute(i, price, new Advertisments(tv, internet, magazines));
			XYChart.Data<Number, Number> dataTemp = new XYChart.Data<Number, Number>(i,precSold);
			series.getData().add(dataTemp);

		}
		
		precSoldChart.getData().add(series);
		series.getNode().setStyle("-fx-stroke: BLUE;");
	}

}