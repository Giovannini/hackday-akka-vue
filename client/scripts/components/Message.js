import Vue from 'vue';

export const Message = Vue.component('message', {
  props: [],
  data: () => ({
    message: '',
    fullMessage: 'My name is Thomas',
    seen: true,
    tooltipMessage: 'Hello Vue!',
    i: 0,
  }),
  template: `\
    <div>\
      <input v-model="tooltipMessage">\
      <br/>\
      <button v-on:click="newLetter">New letter</button>\
      <button v-on:click="reverseMessage">Reverse Message</button>\
      <button v-on:click="clearMessage">Clear Message</button>\
      <button v-on:click="hideMessage" v-if="seen">Hide Message</button>\
      <button v-on:click="hideMessage" v-if="!seen">Show Message</button>\
      \
      <p v-bind:title="tooltipMessage" v-if="seen">\
        {{ message }}\
      </p>\
    </div>\
  `,
  methods: {
    reverseMessage: function() {
      this.message = this.message.split('').reverse().join('')
    },
    newLetter: function() {
      if (this.i < this.fullMessage.length) {
        this.message = this.message + this.fullMessage[this.i];
        this.i++;
      }
    },
    clearMessage: function() {
      this.i = 0;
      this.message = '';
    },
    hideMessage: function() {
      this.seen = !this.seen;
    }
  }
});
