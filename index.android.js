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

export default class reactNativeAndroidWearDemo extends Component {
  constructor(props) {
    super(props);
    this.state = {
      counter: 0
    };
  };

  componentWillMount() {
    DeviceEventEmitter.addListener(INCREASE_COUNTER_EVENT, this.increaseLocalCounter);
  };

  componentWillUnmount() {
    DeviceEventEmitter.removeListener(INCREASE_COUNTER_EVENT, this.increaseLocalCounter);
  };

  increaseLocalCounter = () => {
    const currentValue = this.state.counter;
    this.setState({
      counter: currentValue + 1
    });
  };

  increaseWearCounter = () => {
    NativeModules.AndroidWearCommunication.increaseWearCounter();
  };

  render() {
    return (
      <View style={styles.container}>
        <Text>Counter on phone:</Text>
        <Text style={styles.counter}>{this.state.counter}</Text>
        <View style={styles.buttonContainer}>
          <Button
            title="Increase Wear Counter"
            onPress={this.increaseWearCounter}
            style={styles.button}
          />
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
