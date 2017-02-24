import Vue from 'vue';

const Speaker = Vue.component('speaker', {
  data: () => ({}),
  template: `<div class="speaker"></div>`,
  methods: {
    bump: function() {},
  }
});

const Actor = Vue.component('actor', {
  props: ['text', 'onend'],
  data: () => ({
    ssu: new SpeechSynthesisUtterance(),
  }),
  template: `\
    <div class="actor">\
      <speaker></speaker>\
    </div>\
  `,
  created: function() {
    this.ssu.onend = this.onend;
  },
  watch: {
    text: function (newText) {
      this.readText(newText);
    },
    onend: function(newFunction) {
      this.ssu.onend = newFunction;
    },
  },
  methods: {
    readText: function(text) {
      this.ssu.text = text;
      window.speechSynthesis.speak(this.ssu);
    },
  }
});

export const Play = Vue.component('play', {
  data: () => ({
    connection: new WebSocket('ws://localhost:8080/theatre/romeoEtJuliette'),
    romeoText: '',
    julietText: '',
  }),
  template: `\
    <div class="play-scene">\
      <actor v-bind:onend="this.onSpeakEnd" v-bind:text="this.romeoText"></actor>\
      <actor v-bind:onend="this.onSpeakEnd" v-bind:text="this.julietText"></actor>\
      <button class="starting-button" v-on:click="readStream">\
        Start Romeo and Juliet\
      </button>\
    </div>\
  `,
  methods: {
    onSpeakEnd: function() {
      this.connection.send('Ping');
    },
    readStream: function() {
      console.log("Let's go!");
      window.speechSynthesis.cancel();

      this.connection.send('Ping');
      this.connection.onerror = (error) => console.error('WebSocket Error ', error);

      this.connection.onmessage = (event) => {
        const data = JSON.parse(event.data);
        console.log(data);
        if(data["name"] === "Romeo") this.romeoText = data.text;
        else if(data["name"] === "Juliette") this.julietText = data.text;
        else this.unknownSpeaks();
      };
    },
  }
});
