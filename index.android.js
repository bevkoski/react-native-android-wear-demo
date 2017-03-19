'use strict';

import React, { Component } from 'react';
import {
  AppRegistry,
  NativeModules,
  DeviceEventEmitter,
  View,
  Button,
  Text,
  StyleSheet
} from 'react-native';

const INCREASE_COUNTER_EVENT = 'increaseCounter';
const ENABLE_LAUNCH_CONTROLS_EVENT = 'enableWearAppLaunchControls';
const ENABLE_COUNT_CONTROLS_EVENT = 'enableCountControls';

export default class reactNativeAndroidWearDemo extends Component {
  constructor(props) {
    super(props);
    this.state = {
      counter: 0,
      showLaunchButton: false,
      showCountButton: false
    };
  };

  componentWillMount() {
    this.setState({
      counter: 0,
      showLaunchButton: false,
      showCountButton: false
    });
    DeviceEventEmitter.addListener(INCREASE_COUNTER_EVENT, this.increaseLocalCounter);
    DeviceEventEmitter.addListener(ENABLE_LAUNCH_CONTROLS_EVENT, this.enableLaunchControls);
    DeviceEventEmitter.addListener(ENABLE_COUNT_CONTROLS_EVENT, this.enableCountControls);
  };

  componentWillUnmount() {
    DeviceEventEmitter.removeListener(INCREASE_COUNTER_EVENT, this.increaseLocalCounter);
    DeviceEventEmitter.removeListener(ENABLE_LAUNCH_CONTROLS_EVENT, this.enableLaunchControls);
    DeviceEventEmitter.removeListener(ENABLE_COUNT_CONTROLS_EVENT, this.enableCountControls);
  };

  increaseLocalCounter = () => {
    const currentValue = this.state.counter;
    this.setState({
      counter: currentValue + 1
    });
  };

  enableLaunchControls = () => {
    this.setState({
      showLaunchButton: true,
      showCountButton: false
    });
  };

  enableCountControls = () => {
    this.setState({
      showLaunchButton: false,
      showCountButton: true
    });
  };

  launchWearApp = () => {
    NativeModules.AndroidWearCommunication.launchWearApp();
  };

  increaseWearCounter = () => {
    NativeModules.AndroidWearCommunication.increaseWearCounter();
  };

  render() {
    const launchButton = this.state.showLaunchButton ? <Button
        title="Launch Wear App"
        onPress={this.launchWearApp}
        style={styles.button} /> : null;
    const countButton = this.state.showCountButton ? <Button
        title="Increase Wear Counter"
        onPress={this.increaseWearCounter}
        style={styles.button} /> : null;
    return (
      <View style={styles.container}>
        <Text>Counter on phone:</Text>
        <Text style={styles.counter}>{this.state.counter}</Text>
        <View style={styles.buttonContainer}>
          {launchButton}
          {countButton}
        </View>
      </View>
    );
  };
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF'
  },
  label: {
    fontSize: 60,
  },
  counter: {
    fontSize: 140,
    color: 'black'
  },
  buttonContainer: {
    padding: 35
  },
  button: {
    fontSize: 90
  }
});

AppRegistry.registerComponent('reactNativeAndroidWearDemo', () => reactNativeAndroidWearDemo);
