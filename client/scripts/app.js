import Vue from 'vue';

const appElement = '#app';

let message = '';
let i = 0;
const fullMessage = 'My name is Thomas';

new Vue({
  el: appElement,
  data: {
    message: message,
    seen: true,
    tooltipMessage: 'Hello Vue!'
  },
  methods: {
    reverseMessage: function () {
      this.message = this.message.split('').reverse().join('')
    },
    newLetter: function () {
      if (i < fullMessage.length) {
        this.message = this.message + fullMessage[i];
        i++;
      }
    },
    clearMessage: function () {
      i = 0;
      this.message = '';
    },
    hideMessage: function() {
      this.seen = !this.seen;
    }
  }
});
