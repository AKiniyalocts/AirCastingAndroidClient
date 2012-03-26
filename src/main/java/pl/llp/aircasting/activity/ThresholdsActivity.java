/**
    AirCasting - Share your Air!
    Copyright (C) 2011-2012 HabitatMap, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    You can contact the authors by email at <info@habitatmap.org>
*/
package pl.llp.aircasting.activity;

import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.google.inject.Inject;
import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.SettingsHelper;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/8/11
 * Time: 2:37 PM
 */
public class ThresholdsActivity extends RoboActivity implements View.OnClickListener, View.OnFocusChangeListener, SeekBar.OnSeekBarChangeListener {
    @InjectView(R.id.color_scale_too_loud) EditText tooLoudEdit;
    @InjectView(R.id.color_scale_very_loud) EditText veryLoudEdit;
    @InjectView(R.id.color_scale_loud) EditText loudEdit;
    @InjectView(R.id.color_scale_average) EditText averageEdit;
    @InjectView(R.id.color_scale_quiet) EditText quietEdit;

    @InjectView(R.id.color_scale_very_loud_slider) SeekBar veryLoudSlider;
    @InjectView(R.id.color_scale_loud_slider) SeekBar loudSlider;
    @InjectView(R.id.color_scale_average_slider) SeekBar averageSlider;

    @InjectView(R.id.top_bar_very_high) TextView topBarTooLoud;
    @InjectView(R.id.top_bar_high) TextView topBarVeryLoud;
    @InjectView(R.id.top_bar_mid) TextView topBarLoud;
    @InjectView(R.id.top_bar_low) TextView topBarAverage;
    @InjectView(R.id.top_bar_very_low) TextView topBarQuiet;

    @InjectView(R.id.save) Button save;
    @InjectView(R.id.reset) Button reset;

    @Inject SettingsHelper settingsHelper;
    @Inject Application context;

    private int tooLoud;
    private int veryLoud;
    private int loud;
    private int average;
    private int quiet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_scale);

        tooLoudEdit.setText("" + settingsHelper.getThreshold(MeasurementLevel.VERY_HIGH));
        veryLoudEdit.setText("" + settingsHelper.getThreshold(MeasurementLevel.HIGH));
        loudEdit.setText("" + settingsHelper.getThreshold(MeasurementLevel.MID));
        averageEdit.setText("" + settingsHelper.getThreshold(MeasurementLevel.LOW));
        quietEdit.setText("" + settingsHelper.getThreshold(MeasurementLevel.VERY_LOW));

        veryLoudEdit.setOnFocusChangeListener(this);
        loudEdit.setOnFocusChangeListener(this);
        averageEdit.setOnFocusChangeListener(this);
        tooLoudEdit.setOnFocusChangeListener(this);
        quietEdit.setOnFocusChangeListener(this);

        veryLoudSlider.setOnSeekBarChangeListener(this);
        loudSlider.setOnSeekBarChangeListener(this);
        averageSlider.setOnSeekBarChangeListener(this);

        save.setOnClickListener(this);
        reset.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save:
                saveThresholds();
                break;
            case R.id.reset:
                finish();
                settingsHelper.resetThresholds();
                break;
        }
    }

    private void saveThresholds() {
        calculateThresholds();
        fixThresholds();       

        settingsHelper.setThreshold(MeasurementLevel.VERY_HIGH, tooLoud);
        settingsHelper.setThreshold(MeasurementLevel.HIGH, veryLoud);
        settingsHelper.setThreshold(MeasurementLevel.MID, loud);
        settingsHelper.setThreshold(MeasurementLevel.LOW, average);
        settingsHelper.setThreshold(MeasurementLevel.VERY_LOW, quiet);

        finish();
    }

    private void fixThresholds() {
        View focus = getCurrentFocus();
        int id = focus == null ? 0 : focus.getId();
        MeasurementLevel measurementLevel = toSoundLevel(id);

        fixThresholds(measurementLevel);
    }

    private void calculateThresholds() {
        try {
            tooLoud = tooLoudEdit.getText().length() == 0 ? 0 : Integer.parseInt(tooLoudEdit.getText().toString());
            veryLoud = veryLoudEdit.getText().length() == 0 ? 0 : Integer.parseInt(veryLoudEdit.getText().toString());
            loud = loudEdit.getText().length() == 0 ? 0 : Integer.parseInt(loudEdit.getText().toString());
            average = averageEdit.getText().length() == 0 ? 0 : Integer.parseInt(averageEdit.getText().toString());
            quiet = quietEdit.getText().length() == 0 ? 0 : Integer.parseInt(quietEdit.getText().toString());
        } catch (NumberFormatException e) {
            updateEdits();
            Toast.makeText(context, R.string.setting_error, Toast.LENGTH_LONG).show();
        }
    }

    private void updateEdits() {
        tooLoudEdit.setText("" + tooLoud);
        veryLoudEdit.setText("" + veryLoud);
        loudEdit.setText("" + loud);
        averageEdit.setText("" + average);
        quietEdit.setText("" + quiet);
    }

    private MeasurementLevel toSoundLevel(int id) {
        switch (id) {
            case R.id.color_scale_quiet:
                return MeasurementLevel.VERY_LOW;
            case R.id.color_scale_average:
            case R.id.color_scale_average_slider:
                return MeasurementLevel.LOW;
            case R.id.color_scale_loud:
            case R.id.color_scale_loud_slider:
                return MeasurementLevel.MID;
            case R.id.color_scale_very_loud:
            case R.id.color_scale_very_loud_slider:
                return MeasurementLevel.HIGH;
            default:
                return MeasurementLevel.VERY_HIGH;
        }
    }

    private void fixThresholds(MeasurementLevel fixed) {
        switch (fixed) {
            case VERY_LOW:
                if (average <= quiet) average = quiet + 1;
            case LOW:
                if (loud <= average) loud = average + 1;
            case MID:
                if (veryLoud <= loud) veryLoud = loud + 1;
            case HIGH:
                if (tooLoud <= veryLoud) tooLoud = veryLoud + 1;
        }

        switch (fixed) {
            case VERY_HIGH:
                if (veryLoud >= tooLoud) veryLoud = tooLoud - 1;
            case HIGH:
                if (loud >= veryLoud) loud = veryLoud - 1;
            case MID:
                if (average >= loud) average = loud - 1;
            case LOW:
                if (quiet >= average) quiet = average - 1;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        calculateThresholds();
        fixThresholds(toSoundLevel(view.getId()));
        updateViews();
    }

    private void updateViews() {
        updateSliders();
        updateEdits();
        updateTopBar();
    }

    private void updateTopBar() {
        topBarQuiet.setText("" + quiet);
        topBarAverage.setText("" + average);
        topBarLoud.setText("" + loud);
        topBarVeryLoud.setText("" + veryLoud);
        topBarTooLoud.setText("" + tooLoud);
    }

    private void updateSliders() {
        veryLoudSlider.setProgress((int) (veryLoudSlider.getMax() * project(veryLoud)));
        loudSlider.setProgress((int) (loudSlider.getMax() * project(loud)));
        averageSlider.setProgress((int) (averageSlider.getMax() * project(average)));
    }

    private double project(double value) {
        return (value - quiet) / (tooLoud - quiet);
    }

    @Override
    public void onProgressChanged(SeekBar bar, int i, boolean b) {
        int value = unProject(bar.getProgress(), bar.getMax());
        MeasurementLevel measurementLevel = toSoundLevel(bar.getId());
        switch (measurementLevel) {
            case LOW:
                averageEdit.setText("" + value);
                break;
            case MID:
                loudEdit.setText("" + value);
                break;
            case HIGH:
                veryLoudEdit.setText("" + value);
                break;
        }
    }

    private int unProject(int progress, int max) {
        return (int) (quiet + progress / (double) max * (tooLoud - quiet));
    }

    @Override
    public void onStartTrackingTouch(SeekBar bar) {
        bar.requestFocus();
    }

    @Override
    public void onStopTrackingTouch(SeekBar bar) {
        calculateThresholds();
        MeasurementLevel measurementLevel = toSoundLevel(bar.getId());
        fixThresholds(measurementLevel);
        updateViews();
    }
}