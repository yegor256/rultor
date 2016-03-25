module Rultor
  class ContentsBlock < Liquid::Tag
    def render(context)
      html = ''
      context['site']['posts'].each do |post|
        html += "<p><a href='#{post.url}'>#{post.title}</a></p>"
      end
      html
    end
  end
end

Liquid::Template.register_tag("contents", Rultor::ContentsBlock)
